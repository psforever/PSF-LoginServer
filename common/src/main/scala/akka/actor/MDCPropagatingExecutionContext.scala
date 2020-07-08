package akka.actor

// Taken from https://medium.com/hootsuite-engineering/logging-contextual-info-in-an-asynchronous-scala-application-8ea33bfec9b3

import org.slf4j.MDC

import scala.concurrent.ExecutionContext

trait MDCPropagatingExecutionContext extends ExecutionContext {
  // name the self-type "self" so we can refer to it inside the nested class
  self =>

  override def prepare(): ExecutionContext =
    new ExecutionContext {
      // Save the call-site MDC state
      val context = MDC.getCopyOfContextMap

      def execute(r: Runnable): Unit =
        self.execute(new Runnable {
          def run(): Unit = {
            // Save the existing execution-site MDC state
            val oldContext = MDC.getCopyOfContextMap
            try {
              // Set the call-site MDC state into the execution-site MDC
              if (context != null)
                MDC.setContextMap(context)
              else
                MDC.clear()

              r.run()
            } finally {
              // Restore the existing execution-site MDC state
              if (oldContext != null)
                MDC.setContextMap(oldContext)
              else
                MDC.clear()
            }
          }
        })

      def reportFailure(t: Throwable): Unit = self.reportFailure(t)
    }
}

object MDCPropagatingExecutionContext {
  object Implicits {
    // Convenience wrapper around the Scala global ExecutionContext so you can just do:
    // import MDCPropagatingExecutionContext.Implicits.global
    implicit lazy val global = MDCPropagatingExecutionContextWrapper(ExecutionContext.Implicits.global)
  }
}

/**
  * Wrapper around an existing ExecutionContext that makes it propagate MDC information.
  */
class MDCPropagatingExecutionContextWrapper(wrapped: ExecutionContext)
    extends ExecutionContext
    with MDCPropagatingExecutionContext {

  override def execute(r: Runnable): Unit = wrapped.execute(r)

  override def reportFailure(t: Throwable): Unit = wrapped.reportFailure(t)
}

object MDCPropagatingExecutionContextWrapper {
  def apply(wrapped: ExecutionContext): MDCPropagatingExecutionContextWrapper = {
    new MDCPropagatingExecutionContextWrapper(wrapped)
  }
}
