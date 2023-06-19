package com.gregorpurdy.ident

object ISINTestMain {
  val isinString = "US0378331005"
  def main(args: Array[String]): Unit = {
    ISIN.parse(isinString) match {
      case Right(isin) =>
        println(s"Parsed ISIN: $isin"); // "US0378331005"
        println(s"  Country code: ${isin.countryCode}"); // "US"
        println(s"  Security identifier: ${isin.securityIdentifier}"); // "037833100"
        println(s"  Check digit: ${isin.checkDigit}"); // '5'
      case Left(err) =>
        throw new RuntimeException(s"Unable to parse ISIN $isinString: $err")
    }
  }
}
