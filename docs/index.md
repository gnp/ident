# Ident

![CI][Badge-CI] [![Sonatype
Releases](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fgregorpurdy%2Fident_2.13%2Fmaven-metadata.xml&label=Sonatype%20Release)](https://repo1.maven.org/maven2/com/gregorpurdy/ident/ident_2.13/)

The `ident` library contains Scala classes for working with identifiers of
various types that have been validated to have the correct format:

* [ISIN](https://github.com/gnp/isin-sc/): International Securities
  Identification Number (ISO 6166:2021)


## ISIN

International Security Identifiers (ISINs) as defined in [ISO 6166:2021
Financial services — International securities identification number
(ISIN)](https://www.iso.org/standard/78502.html) ("The Standard").


## Example

```scala
import com.gregorpurdy.ident.Isin

object IsinTestMain {
  val isinString = "US0378331005"
  def main(args: Array[String]): Unit = {
    Isin.parse(isinString) match {
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


## Usage

Add this to your `build.sbt`:

```scala
libraryDependencies += "com.gregorpurdy" %% "ident" % @VERSION@
```

for the basic identifier data types, or use one or more of the integrations to
support various encodings:

```scala
libraryDependencies += "com.gregorpurdy" %% "ident-circe" % @VERSION@
libraryDependencies += "com.gregorpurdy" %% "ident-zio-json" % @VERSION@
libraryDependencies += "com.gregorpurdy" %% "ident-zio-schema" % @VERSION@
```


## JSON

You can use the `isin-circe` or `isin-zio-json` artifacts to get JSON encoders
and decoders for ISINs.

You can also use the `isin-zio-schema` artifact to get generic ZIO Schema
support, which includes support for JSON and other formats as well as other
functionality.


## License

Licensed under Apache License, Version 2.0 ([LICENSE-APACHE][LICENSE-APACHE] or
http://www.apache.org/licenses/LICENSE-2.0)


## Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
licensed as above, without any additional terms or conditions.


## Copyright

Copyright 2023 Gregor Purdy. All rights reserved.

[Badge-CI]: https://github.com/gnp/ident/workflows/CI/badge.svg
[LICENSE-APACHE]: https://github.com/gnp/ident/blob/master/LICENSE-APACHE
