package net.psforever.api

import java.net.InetAddress

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.headers.`X-Forwarded-For`

/**
  * Parse an IP address literal without ever resolving a host name.
  *
  * `InetAddress.getByName` falls back to a DNS lookup for anything it does not recognise as a literal,
  * which on a request path would let a caller make the server query DNS. Akka HTTP's own
  * `X-Forwarded-For` grammar accepts IPv4 and IPv6 literals only and builds the address from bytes,
  * so it is reused here as a literal-only parser.
  */
object IpLiteral {
  def parse(text: String): Option[InetAddress] =
    HttpHeader.parse(`X-Forwarded-For`.name, text.trim) match {
      case ParsingResult.Ok(header: `X-Forwarded-For`, Nil) if header.addresses.size == 1 =>
        header.addresses.head.toIP.map(_.ip)
      case _ => None
    }
}

/**
  * An address range in CIDR notation, or a single address.
  *
  * @param network the range's address bytes, already masked to the prefix
  * @param prefix  the number of leading bits that must match
  */
final case class Cidr(network: Array[Byte], prefix: Int) {
  def contains(address: InetAddress): Boolean = {
    val bytes = address.getAddress
    bytes.length == network.length && Cidr.mask(bytes, prefix).sameElements(network)
  }
}

object Cidr {

  /** Parse `10.0.0.0/8`, `fd00::/8`, or a bare address (a single-address range). */
  def parse(text: String): Either[String, Cidr] = {
    val (addressText, prefixText) = text.trim.split("/", 2) match {
      case Array(a, p) => (a, Some(p))
      case Array(a)    => (a, None)
    }
    IpLiteral.parse(addressText) match {
      case None => Left(s"'$text' is not an IP address or CIDR range")
      case Some(address) =>
        val bits = address.getAddress.length * 8
        prefixText.map(p => p.toIntOption.filter(n => n >= 0 && n <= bits)) match {
          case Some(None)         => Left(s"'$text' has an invalid prefix length (expected 0 to $bits)")
          case Some(Some(prefix)) => Right(Cidr(mask(address.getAddress, prefix), prefix))
          case None               => Right(Cidr(address.getAddress, bits))
        }
    }
  }

  private[api] def mask(bytes: Array[Byte], prefix: Int): Array[Byte] =
    bytes.zipWithIndex.map {
      case (b, i) =>
        val keep = math.max(0, math.min(8, prefix - i * 8))
        (b & (0xff << (8 - keep))).toByte
    }
}

/**
  * Decides which address a request really came from.
  *
  * `X-Forwarded-For` is written by whoever sends the request, so it is believed only when the
  * connection itself comes from a configured proxy. Even then only the hops that proxy (and any other
  * trusted proxy) appended can be relied on, so the list is read from the right: the first address
  * that is not a trusted proxy is the client. Anything to the left of it was supplied by the client and
  * is ignored. An entry that is not an IP literal stops the walk, and the last trusted hop is used.
  */
final class TrustedProxies(ranges: Seq[Cidr]) {
  def isTrusted(address: InetAddress): Boolean = ranges.exists(_.contains(address))

  def clientOf(peer: InetAddress, forwardedFor: Option[String]): InetAddress =
    if (!isTrusted(peer)) peer
    else {
      val hops = forwardedFor.toList.flatMap(_.split(',')).map(_.trim).filter(_.nonEmpty).reverse
      hops
        .foldLeft[(InetAddress, Boolean)]((peer, false)) {
          case (found @ (_, true), _) => found
          case ((last, false), hop) =>
            IpLiteral.parse(hop) match {
              case Some(address) if isTrusted(address) => (address, false)
              case Some(address)                       => (address, true)
              case None                                => (last, true)
            }
        }
        ._1
    }
}

object TrustedProxies {

  /** Parse the configured ranges, reporting every entry that is not valid. */
  def parse(entries: Seq[String]): Either[String, TrustedProxies] = {
    val parsed = entries.map(Cidr.parse)
    val errors = parsed.collect { case Left(e) => e }
    if (errors.nonEmpty) Left(s"api.trusted-proxies: ${errors.mkString("; ")}")
    else Right(new TrustedProxies(parsed.collect { case Right(c) => c }))
  }
}
