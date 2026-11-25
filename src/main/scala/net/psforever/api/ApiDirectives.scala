package net.psforever.api

import java.net.InetAddress

import akka.http.scaladsl.model.headers.{CacheDirectives, RawHeader, `Cache-Control`}
import akka.http.scaladsl.model.{AttributeKeys, ContentTypes, HttpEntity, HttpResponse, StatusCode}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1, ExceptionHandler, RejectionHandler, Route}
import org.json4s.native.Serialization.write
import org.json4s.{DefaultFormats, Formats}

/** Behaviour shared by both surfaces of the PSF-HTTP API. */
object ApiDirectives {
  private val log = org.log4s.getLogger

  implicit val formats: Formats = DefaultFormats

  /** A JSON response. */
  def json(status: StatusCode, value: Any): HttpResponse =
    HttpResponse(status, entity = HttpEntity(ContentTypes.`application/json`, write(value)))

  /** An error, in the one shape every error from this API takes. */
  def error(status: StatusCode, message: String): HttpResponse =
    json(status, Map("message" -> message, "error" -> true))

  /**
    * Headers on every response. The API serves JSON only, so a browser is told never to sniff it as
    * anything else, render it in a frame, or load anything on its behalf. Responses are not cacheable
    * unless a route says otherwise.
    */
  private val defaultHeaders = List(
    RawHeader("X-Content-Type-Options", "nosniff"),
    RawHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"),
    RawHeader("Referrer-Policy", "no-referrer"),
    `Cache-Control`(CacheDirectives.`no-store`)
  )

  /**
    * Rejections (unknown path, wrong method, oversized request, ...) answered as JSON errors. Akka's
    * own messages are kept: they describe the request, never the server.
    */
  private val rejections: RejectionHandler =
    RejectionHandler.default.mapRejectionResponse {
      case response @ HttpResponse(status, _, entity: HttpEntity.Strict, _)
          if entity.contentType != ContentTypes.`application/json` =>
        response.withEntity(
          HttpEntity(ContentTypes.`application/json`, write(Map("message" -> entity.data.utf8String, "error" -> true)))
        )
      case other => other
    }

  /** An unexpected failure is logged here and reported to the caller without any detail. */
  private val exceptions: ExceptionHandler = ExceptionHandler {
    case e: Throwable =>
      log.error(e)("PSF-HTTP API request failed")
      complete(error(akka.http.scaladsl.model.StatusCodes.InternalServerError, "internal error"))
  }

  /** Wrap a surface's routes: default headers, JSON rejections, and no leaked exceptions. */
  def surface(route: Route): Route =
    respondWithDefaultHeaders(defaultHeaders) {
      handleExceptions(exceptions) {
        handleRejections(rejections) {
          route
        }
      }
    }

  /** Mark a successful response as cacheable by anyone for `seconds`. */
  def publicCache(seconds: Long): Directive0 =
    respondWithHeader(`Cache-Control`(CacheDirectives.public, CacheDirectives.`max-age`(seconds)))

  /**
    * The address of the TCP peer, as the server saw it. Requires the `remote-address-attribute` server
    * setting, which [[ApiServer]] turns on. Never derived from a request header.
    */
  val peerAddress: Directive1[InetAddress] =
    extractRequest.map { request =>
      request
        .attribute(AttributeKeys.remoteAddress)
        .flatMap(_.toIP)
        .map(_.ip)
        .getOrElse(ApiDirectives.UnknownAddress)
    }

  /** Stands in for a peer whose address could not be read, so it still shares a rate-limit bucket. */
  val UnknownAddress: InetAddress = InetAddress.getByAddress(Array[Byte](0, 0, 0, 0))
}
