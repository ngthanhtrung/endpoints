package cqrs.publicserver

import play.api.routing.{Router => PlayRouter}

class Router(publicServer: documented.PublicServer) {

  val routes: PlayRouter.Routes =
    BootstrapEndpoints.routes orElse publicServer.routes

}
