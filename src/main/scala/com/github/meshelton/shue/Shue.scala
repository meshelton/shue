package com.github.meshelton.shue

//import io.circe.generic.auto._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, RequestEntity, ResponseEntity}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.{ExecutionContext, Future}

case class IPAddress(address: String)


class Shue(val bridgeApiIpAddress: IPAddress, val username: String, val actorSystem: ActorSystem) {

  import ShueProtocol._

  implicit val ac = actorSystem
  implicit val materializer = ActorMaterializer()

  def makeApiCall[T](request: HttpRequest)(implicit um: Unmarshaller[ResponseEntity, T], ec: ExecutionContext = null, mat: Materializer): Future[T] = {
    Http().singleRequest(request).flatMap(resp ⇒ Unmarshal(resp.entity).to[T])
  }

  val apiBase = s"http://${bridgeApiIpAddress.address }/api/$username"

  // https://www.developers.meethue.com/documentation/lights-api
  class Lights {
    val resourceBase = s"$apiBase/lights"


    def getAll: Future[Map[String, Light]] = makeApiCall(HttpRequest(uri = resourceBase))

    def apply = getAll

    def get(lightId: Int): Future[Light] = makeApiCall(HttpRequest(uri = s"$resourceBase/$lightId"))

    def apply(lightId: Int) = get _

    def getNew: Future[Map[String, Either[String, Light]]] = makeApiCall(HttpRequest(uri = s"$resourceBase/new"))

    def searchForNew: Future[Unit] = makeApiCall(HttpRequest(uri = resourceBase, method = POST))

    def searchForNew(searchRequest: LightSearchRequest): Future[Unit] = {
      Marshal(searchRequest)
        .to[RequestEntity]
        .flatMap(e ⇒ makeApiCall(HttpRequest(uri = resourceBase, method = POST, entity = e)))
    }

    def rename(lightId: Int, newName: String): Future[String] = {
      Marshal(newName).to[RequestEntity]
        .flatMap(e ⇒ makeApiCall[Map[String, Map[String, String]]](HttpRequest(uri = resourceBase, method = PUT, entity = e)))
        .map(_ ("success")(s"/lights/$lightId/name"))
    }

    // TODO(mshelton): Actually encode the success response here instead of this mapmapmap...
    def setState(lightId: Int, lightState: LightState): Future[Map[String, Map[String, String]]] = Marshal(lightState)
      .to[RequestEntity]
      .flatMap(e ⇒ makeApiCall(HttpRequest(uri = s"$resourceBase/$lightId/state", method = PUT, entity = e)))

    def apply(lightId: Int, lightState: LightState) = setState _
  }

  class Groups {
    val resourceBase = s"$apiBase/groups"
  }

  class Config {
    val resourceBase = s"$apiBase/config"
  }

  class Schedules {
    val resourceBase = s"$apiBase/schedules"
  }

  class Scenes {
    val resourceBase = s"$apiBase/scenes"
  }

  class Sensors {
    val resourceBase = s"$apiBase/sensors"
  }

  class Rules {
    val resourceBase = s"$apiBase/rules"
  }


}