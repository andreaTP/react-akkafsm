import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom
import japgolly.scalajs.react.vdom.html_<^._

import akka.actor._
import scala.concurrent.duration._

object Main extends App {

  // FROM
  // https://japgolly.github.io/scalajs-react/#examples/timer
  // https://doc.akka.io/docs/akka/2.5/fsm.html

  sealed trait ReactState
  case object InitialTime extends ReactState
  case class Time(secondsElapsed: Long) extends ReactState


  sealed trait InternalState
  case object Init extends InternalState
  case object Active extends InternalState

  final case object Start

  val system = ActorSystem()

  class FSMBackend(val bs: BackendScope[Unit, ReactState]) {

    class StateBackend() extends FSM[InternalState, ReactState] {

      startWith(Init, InitialTime)

      when(Init) {
        case Event(Start, InitialTime) =>
          val nextTime = Time(0)
          bs.setState(nextTime).runNow()
          goto(Active) using nextTime
      }

      when(Active, stateTimeout = 1 second) {
        case Event(StateTimeout, Time(old)) =>
          val nextTime = Time(old + 1)
          bs.setState(nextTime).runNow()
          stay using nextTime
      }
    }

    val state = system.actorOf(Props(new StateBackend()))

    def render(s: ReactState): VdomElement = s match {
      case InitialTime =>
        <.div("Initializing")
      case Time(s) =>
        <.div("Seconds elapsed: ", s)
    }

    def start =  Callback { state ! Start }
    def stop =  Callback { state ! PoisonPill }
  }

  val Timer = ScalaComponent.builder[Unit]("Timer")
    .initialState(InitialTime: ReactState)
    .backend(new FSMBackend(_))
    .renderBackend
    .componentDidMount(_.backend.start)
    .componentWillUnmount(_.backend.stop)
    .build

  import org.scalajs.dom.document

  Timer().renderIntoDOM(document.body)
}
