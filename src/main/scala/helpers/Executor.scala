package helpers

import java.util.concurrent.Executors

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

trait Executor {

  implicit def defaultContext: ExecutionContext
}

trait ConfiguredExecutor extends Configurable {

  private final val ThreadPoolExecutorPattern = """^(.*thread-pool-executor.*)$""".r
  private final val ForkJoinPoolExecutorPattern = """^(.*fork-join-executor.*)$""".r

  def lookupExecutionContext(id: String): ExecutionContext = id match {
    case ForkJoinPoolExecutorPattern(path) => ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(configuration.getEssentialInt(path)))
    case ThreadPoolExecutorPattern(path)   => ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(configuration.getEssentialInt(path)))
    case _                                 => ExecutionContext.Implicits.global
  }
}

trait DefaultExecutor {

  implicit def defaultContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}

trait ConfiguredAkkaExecutor {

  def actorSystem: ActorSystem

  def lookupExecutionContext(id: String): ExecutionContext = actorSystem.dispatchers.lookup(id)
}