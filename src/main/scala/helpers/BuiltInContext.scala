package helpers

import akka.actor.ActorSystem
import akka.stream.Materializer

case class BuiltInContext(
  configuration: Configuration,
  actorSystem:   ActorSystem,
  materializer:  Materializer
)

trait BuiltInComponents {

  def builtInContext: BuiltInContext

  implicit def actorSystem: ActorSystem = builtInContext.actorSystem

  implicit def materializer: Materializer = builtInContext.materializer
}
