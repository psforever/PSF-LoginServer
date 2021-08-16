// Copyright (c) 2021 PSForever
package objects

import net.psforever.objects.guid.{Task, TaskBundle, TaskWorkflow}
import org.scalatest.flatspec.AsyncFlatSpec

import scala.concurrent.Future
import scala.util.Failure

class AsyncTaskWorkflowTest extends AsyncFlatSpec {
  case class StringAppendTask(product: StringBuilder, str: String) extends Task {
    def action() = { Future({ product.append(str) }) }
    def undo() = {
      val index = product.indexOf(str)
      product.replace(index, index + str.length, "[successful task undo]")
    }
    def isSuccessful() = { product.indexOf(str) > -1 }
  }

  case class FailedStringAppendTask(product: StringBuilder, str: String) extends Task {
    def action() = { Future(Failure(new Exception("intentional failure"))) }
    def undo() = {
      val index = product.indexOf(str)
      product.replace(index, index + str.length, "[failed task undo]")
    }
    def isSuccessful() = { product.indexOf(str) > -1 }
  }

  behavior of "TaskWorkFlow"

  it should "append a string as a task" in {
    val test: StringBuilder = new StringBuilder()
    assert(test.mkString.isEmpty)
    val result = TaskWorkflow.execute(TaskBundle(StringAppendTask(test, "hello")))
    result map { _ =>
      assert(test.mkString.equals("hello"), "async result does not equal 'hello'")
    }
  }

  it should "append the strings in order of subtask then main task" in {
    val test: StringBuilder = new StringBuilder()
    assert(test.mkString.isEmpty)
    val result = TaskWorkflow.execute(TaskBundle(StringAppendTask(test, " world"), StringAppendTask(test, "hello")))
    result map { _ =>
      assert(test.mkString.equals("hello world"), "async result does not equal 'hello world'")
    }
  }

  it should "append the strings in order of subtasks then main task, with the subtasks being in either order" in {
    val test: StringBuilder = new StringBuilder()
    assert(test.mkString.isEmpty)
    val result = TaskWorkflow.execute(TaskBundle(
      StringAppendTask(test, " world"),
      Seq(
        TaskBundle(StringAppendTask(test, " hello")),
        TaskBundle(StringAppendTask(test, " or goodbye"))
      )
    ))
    result map { _ =>
      val output = test.mkString
      assert(
        output.equals(" or goodbye hello world") || output.equals(" hello or goodbye world"),
        s"async result '$output' does not equal either pattern"
      )
    }
  }

  it should "if a task fails, do not undo it" in {
    val test: StringBuilder = new StringBuilder()
    assert(test.mkString.isEmpty)
    val result = TaskWorkflow.execute(TaskBundle(FailedStringAppendTask(test, " world")))
    result map { _ =>
      val output = test.mkString
      assert(output.equals(""),"async result was written when should have not been written")
      //see implementation of FailedStringAppendTask.undo
    }
  }

  it should "if a middling subtask fails, its parent task will not be executed or undone, but its own subtask will be undone (1)" in {
    val test: StringBuilder = new StringBuilder()
    assert(test.mkString.isEmpty)
    val result = TaskWorkflow.execute(TaskBundle(
      StringAppendTask(test, " world"),
      TaskBundle(FailedStringAppendTask(test, "hello"), StringAppendTask(test, " or goodbye")))
    )
    result map { _ =>
      val output = test.mkString
      assert(output.equals("[successful task undo]"),s"async result, formerly successful, was written as if it had failed - $output")
      //see implementation of StringAppendTask.undo
    }
  }

  it should "if a middling subtask fails, its parent task will not be executed or undone, but its own subtasks will be undone (2)" in {
    val test: StringBuilder = new StringBuilder()
    assert(test.mkString.isEmpty)
    val result = TaskWorkflow.execute(TaskBundle(
      StringAppendTask(test, " world"),
      TaskBundle(FailedStringAppendTask(test, "hello"), List(
        TaskBundle(StringAppendTask(test, " or goodbye")),
        TaskBundle(StringAppendTask(test, " or something"))
      ))
    ))
    result map { _ =>
      val output = test.mkString
      assert(
        output.equals("[successful task undo][successful task undo]"),
        s"async result, formerly successful, was written as if it had failed - $output"
      )
      //see implementation of StringAppendTask.undo
    }
  }
}

object TaskWorkflowTest {
  /** placeholder */
}
