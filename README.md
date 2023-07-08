# ISIN

![CI][Badge-CI] [![Sonatype Releases](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.gregorpurdy.ident/isin_2.13.svg?label=Sonatype%20Release)](https://oss.sonatype.org/content/repositories/releases/com/gregorpurdy/ident/isin_2.13/)

A Scala class for working with validated International Security Identifiers
(ISINs) as defined in [ISO 6166:2021 Financial services — International
securities identification number
(ISIN)](https://www.iso.org/standard/78502.html) ("The Standard").

This library is part of the Financial Identifiers series:

* [ISIN](https://github.com/gnp/isin-sc/): International Securities Identification Number (ISO 6166:2021)


## Usage

Add this to your `build.sbt`:

```scala
libraryDependencies += "com.gregorpurdy.ident" %% "isin" % "0.1.0"
```

for the basic identifier data type, or use one or more of the integrations:

```scala
libraryDependencies += "com.gregorpurdy.ident" %% "isin-circe" % "0.1.0"
libraryDependencies += "com.gregorpurdy.ident" %% "isin-zio-json" % "0.1.0"
libraryDependencies += "com.gregorpurdy.ident" %% "isin-zio-schema" % "0.1.0"
```


## Example

```scala
import com.gregorpurdy.ident.ISIN

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
```


## JSON

You can use the `isin-circe` or `isin-zio-json` artifacts to get JSON encoders and
decoders for ISINs.

You can also use the `isin-zio-schema` artifact to get generic ZIO Schema
support, which includes support for JSON and other formats as well as other
functionality.


## License

Licensed under Apache License, Version 2.0 ([LICENSE-APACHE](LICENSE-APACHE) or
http://www.apache.org/licenses/LICENSE-2.0)


## Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
licensed as above, without any additional terms or conditions.


## Copyright

Copyright 2023 Gregor Purdy. All rights reserved.

[Badge-CI]: https://github.com/gnp/isin-sc/workflows/CI/badge.svg
