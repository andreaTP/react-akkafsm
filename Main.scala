import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom
import japgolly.scalajs.react.vdom.html_<^._

import akka.actor._
import scala.concurrent.duration._

// object Main extends App {
//
//   // FROM
//   // https://japgolly.github.io/scalajs-react/#examples/timer
//   // https://doc.akka.io/docs/akka/2.5/fsm.html
//
//   sealed trait ReactState
//   case object InitialTime extends ReactState
//   case class Time(secondsElapsed: Long) extends ReactState
//
//
//   sealed trait InternalState
//   case object Init extends InternalState
//   case object Active extends InternalState
//
//   final case object Start
//
//   val system = ActorSystem()
//
//   class FSMBackend(val bs: BackendScope[Unit, ReactState]) {
//
//     class StateBackend() extends FSM[InternalState, ReactState] {
//
//       startWith(Init, InitialTime)
//
//       when(Init) {
//         case Event(Start, InitialTime) =>
//           val nextTime = Time(0)
//           bs.setState(nextTime).runNow()
//           goto(Active) using nextTime
//       }
//
//       when(Active, stateTimeout = 1 second) {
//         case Event(StateTimeout, Time(old)) =>
//           val nextTime = Time(old + 1)
//           bs.setState(nextTime).runNow()
//           stay using nextTime
//       }
//     }
//
//     val state = system.actorOf(Props(new StateBackend()))
//
//     def render(s: ReactState): VdomElement = s match {
//       case InitialTime =>
//         <.div("Initializing")
//       case Time(s) =>
//         <.div("Seconds elapsed: ", s)
//     }
//
//     def start =  Callback { state ! Start }
//     def stop =  Callback { state ! PoisonPill }
//   }
//
//   val Timer = ScalaComponent.builder[Unit]("Timer")
//     .initialState(InitialTime: ReactState)
//     .backend(new FSMBackend(_))
//     .renderBackend
//     .componentDidMount(_.backend.start)
//     .componentWillUnmount(_.backend.stop)
//     .build
//
//   import org.scalajs.dom.document
//
//   Timer().renderIntoDOM(document.body)
// }

sealed trait FSMState
case object FSMInit extends FSMState

object Main extends App {
  val system = ActorSystem.create("ActorSystemFSM")

  sealed trait ReactState
  case object Initial extends ReactState

  class Backend(bs: BackendScope[Unit, ReactState]) {

    class FSMBackend() extends FSM[FSMState, ReactState] {

      println("123")

      startWith( FSMInit, Initial )

      when(FSMInit) {
        case Event(_, _) => {
          println("mensage Start!!!!!!!!!!!!!")
          stay
        }
      }
      whenUnhandled {
        case Event(_, _) => {
          println("Start*****")
          stay
        }
      }
      initialize()
    }

    val fsm = system.actorOf( Props(new FSMBackend())/*, name = "FSM"*/)

    def onStart = Callback {
      fsm ! "probando"
      println("mmmm")
    }

    def	render(reactState: ReactState) = <.div("Imprimir", <.button(^.tpe := "button", ^.onClick --> Callback{ fsm ! "doSomething" } ))

  }

  val component = ScalaComponent.builder[Unit]("frmPartida")
    .initialState[ReactState](Initial)
    .renderBackend[Backend]
    .build

  import org.scalajs.dom.document

  component().renderIntoDOM(document.body)

}
