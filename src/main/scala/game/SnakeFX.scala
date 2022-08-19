package game

import scalafx.application.{JFXApp3, Platform}
import scalafx.beans.property.{IntegerProperty, ObjectProperty}
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle

import zio._
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SnakeFX extends JFXApp3 {
  type Snake = List[(Double, Double)]
  type Food = (Double, Double)

  val initialSnake: Snake =
    List(
      (250, 200),
      (225, 200),
      (200, 200)
    )

  def gameLoop(
      update: () => Unit
  )(using executor: Executor): UIO[Unit] =
    (ZIO.succeed(update()) *> ZIO.sleep(66.millis)).onExecutor(executor).onDone(e => ZIO.unit, _ => gameLoop(update))

  def gameLoop_v2(update: () => Unit): UIO[Unit] =
    (ZIO.sleep(66.millis) *> ZIO.succeed(update())).onDone(e => ZIO.unit, _ => gameLoop_v2(update))

  case class State(snake: Snake, food: Food) {
    def newState(dir: Int): State = {
      val (x, y) = snake.head
      val (newX, newY) = dir match {
        case 1 => (x, y - 25) // Up
        case 2 => (x, y + 25) // Down
        case 3 => (x - 25, y) // Left
        case 4 => (x + 25, y) // Right
      }
      val newSnake =
        if (
          newX < 0 || newX > 600 || newY < 0 || newY > 600 || snake.tail
            .contains((newX, newY))
        )
          initialSnake
        else if (food == (newX, newY))
          food :: snake
        else
          (newX, newY) :: snake.init

      val newFood =
        if (food == (newX, newY))
          randomFood()
        else
          food
      State(newSnake, newFood)
    }

    def rectangles: List[Rectangle] =
      square(food._1, food._2, Red) :: snake.map { case (x, y) =>
        square(x, y, Green)
      }
  }

  def randomFood(): (Double, Double) =
    (scala.util.Random.nextInt(24) * 25, scala.util.Random.nextInt(24) * 25)
  // def randomFood(): ZIO[Any, Nothing, (Double, Double)] =
  //   for {
  //     x <- Random.nextIntBounded(24).map(_.toDouble * 25)
  //     y <- Random.nextIntBounded(24).map(_.toDouble * 25)
  //   } yield (x, y)

  def square(xr: Double, yr: Double, color: Color): Rectangle = new Rectangle {
    x = xr
    y = yr
    width = 25
    height = 25
    fill = color
  }

  override def start(): Unit = {
    val runtime = Runtime.default
    val state = ObjectProperty(State(initialSnake, randomFood()))
    val frame = IntegerProperty(0)
    val direction = IntegerProperty(4) // 4 = right

    frame.onChange {
      state.update(state.value.newState(direction.value))
    }

    stage = new JFXApp3.PrimaryStage {
      width = 600
      height = 600
      scene = new Scene {
        fill = Black
        content = state.value.rectangles
        onKeyPressed = key =>
          key.getText match {
            case "w" => direction.value = 1
            case "s" => direction.value = 2
            case "a" => direction.value = 3
            case "d" => direction.value = 4
          }

        state.onChange(Platform.runLater {
          content = state.value.rectangles
        })
      }
    }

    // given executor: ExecutorService = Executors.newFixedThreadPool(1)
    given trace: Trace = Trace.empty
    // Unsafe.unsafe {
    //   runtime.unsafe.run(
    //     gameLoop(() => frame.update(frame.value + 1))
    //   )
    // }
    given executor: Executor = Executor.fromJavaExecutor(Executors.newFixedThreadPool(1))
    Unsafe.unsafe {
      runtime.unsafe.run(
        gameLoop(() => frame.update(frame.value + 1))
      )
    }
  }
}
