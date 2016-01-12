package repositories

import context.{MongoConfigurations, Futures, Neo4jConfigurations}
import models._
import org.anormcypher.CypherParser._
import org.anormcypher._
import reactivemongo.bson.BSONDocument

import scala.collection.mutable
import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 02/01/16
  */
trait RulesComponent {

  val rulesRepository: Rules

  trait Rules {

    def rules(): Future[Seq[RuleSystem]]

    def rule(name: String): Future[Option[RuleSystem]]

    def options(system: String, name: String): Future[Seq[RuleOption]]

  }

}

trait DefaultRulesComponentNeo extends RulesComponent {
  self: Neo4jConfigurations with Futures =>

  override val rulesRepository: Rules = new DefaultRules {}

  trait DefaultRules extends Rules {

    override def rules(): Future[Seq[RuleSystem]] = {
      Cypher(
        """
          MATCH (system:RuleSystem) RETURN system.name as system
        """).asAsync {
        str("system") *
      }.map(items => {
        items.map(RuleMapper.strToRuleSystem)
      })
    }

    override def rule(name: String): Future[Option[RuleSystem]] = {
      Cypher(
        """
          MATCH (system:RuleSystem {name: {system}})<-[:BELONGS_TO]-(rule:Rule)-[of:CHILD_OF]->(parent),
          (options:RuleOptions)-[:BELONGS_TO]->(:RuleSystem {name: {system}})
          return collect(options.name) as options, system.name as system, rule, parent.uid as parent, of.idx as idx ORDER BY parent, idx
        """
      ).on("system" -> name).
        asAsync {
          get[Seq[String]]("options") ~ str("system") ~ node("rule") ~ get[Option[String]]("parent") ~ int("idx") *
        }.map(items => {
        RuleMapper.ruleSystemMapper(items.map(flatten))
      })
    }

    override def options(system: String, name: String): Future[Seq[RuleOption]] = {
      Cypher(
        """
         MATCH (system:RuleSystem {name: {system}})<-[:BELONGS_TO]-(opt: RuleOptions {name: {name}})-[:LINK*0..]->(item: RuleOptionItem)
         RETURN item
        """
      ).on("system" -> system, "name" -> name).
        asAsync {
          node("item") *
        }.map(items => {
        items.map(RuleMapper.nodeToOption)
      })
    }
  }

}

object RuleMapper {

  /**
    * transform tuple to rulesystem
    * @param items items => (links, system, rule, parent)
    * @return
    */
  def ruleSystemMapper(items: List[(Seq[String], String, NeoNode, Option[String], Int)]): Option[RuleSystem] = {
    if (items.isEmpty) None
    else {
      val rootItems = mutable.ListBuffer[String]()
      val rules = mutable.HashMap[String, NeoNode]()
      val linkParentRules = mutable.HashMap[String, mutable.ListBuffer[String]]()
      items.foreach(i => {
        val currentUid = propTo(i._3, "uid", "")
        if (i._4.isEmpty) {
          rootItems += currentUid
        } else {
          linkParentRules.getOrElseUpdate(i._4.get, mutable.ListBuffer[String]()) += currentUid
        }
        rules += currentUid -> i._3
      })
      def mapper: String => Rule = id => {
        nodeToRule(rules.get(id).get).copy(rules = linkParentRules.get(id)
          .map(l => l.toList.map(mapper))
          .getOrElse(List()))
      }
      Some(tupleToRuleSystem((items.head._1, items.head._2)).copy(rules = rootItems.map(mapper).toList))
    }
  }

  def strToRuleSystem(item: String): RuleSystem = {
    RuleSystem(item)
  }

  def tupleToRuleSystem(item: (Seq[String], String)): RuleSystem = {
    RuleSystem(item._2, List(), item._1.toList)
  }

  def nodeToRule(node: NeoNode): Rule = {
    Rule(
      propTo(node, "uid", ""),
      propTo(node, "name", ""),
      propTo(node, "category", ""),
      List(),
      node.props.get("links").map(o => listToLinkList(o.asInstanceOf[mutable.ListBuffer[String]].toList)).getOrElse(Map()),
      node.props.get("meta").map(o => listToMeta(o.asInstanceOf[mutable.ListBuffer[String]].toList)).getOrElse(Meta())
    )
  }

  def nodeToOption(node: NeoNode): RuleOption = {
    RuleOption(propTo(node, "name", ""))
  }

  def listToMeta(meta: List[String]): Meta = {
    var result = Meta()
    val minPattern = "min:(\\d+)".r
    val maxPattern = "max:(\\d+)".r
    val freeCategoryPattern = "freeCategory:([a-z]+)".r
    meta.foreach(c => {
      result = c match {
        case "withFree" => result.copy(withFree = Some(true))
        case freeCategoryPattern(cat) => result.copy(freeCategory = Some(cat))
        case minPattern(min) => result.copy(min = Some(min.toInt))
        case maxPattern(max) => result.copy(max = Some(max.toInt))
        case _ => result
      }
    })
    result
  }

  def listToLinkList(list: List[String]): Map[String, String] = {
    val linkPattern = "([a-z]+):([a-zA-Z0-9]+)".r
    list.map(c => c match {
      case linkPattern(name, ref) => (name, ref)
    }).toMap
  }

  def propTo[A](node: NeoNode, name: String, default: A): A = {
    node.props.getOrElse(name, default).asInstanceOf[A]
  }

}