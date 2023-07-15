# Ident

![CI][Badge-CI] [![Sonatype
Releases](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fgregorpurdy%2Fident_2.13%2Fmaven-metadata.xml&label=Sonatype%20Release)](https://repo1.maven.org/maven2/com/gregorpurdy/ident/ident_2.13/)

The `ident` library contains Scala classes for working with identifiers of
various types that have been validated to have the correct format:

* **CIK**: An SEC / EDGAR Central Index Key (CIK) number is a 10-digit numerical
identifier associated with every entity that files with the SEC.

* **CUSIP**: An identifier from [Committee on Uniform Security Identification
Procedures](https://www.cusip.com/identifiers.html?section=CUSIP) (CUSIP) for
North American securities identification.

* **FIGI**: A [Financial Instrument Global
Identifier](https://www.openfigi.com/about/figi) (FIGI).

* **ISIN**: International Security Identifiers (ISINs) as defined in [ISO
6166:2021 Financial services — International securities identification number
(ISIN)](https://www.iso.org/standard/78502.html) ("The Standard").

* **LEI**: An ISO standard (ISO 17442-1:2020(E)) [Legal Entity
Identifier](https://www.gleif.org/en/about-lei/introducing-the-legal-entity-identifier-lei)
(LEI).

* **MIC**: A (Financial) [Market Identifier
  Code](https://en.wikipedia.org/wiki/Market_Identifier_Code) (MIC).


## Example


```scala
val input = "US0378331005"
// input: String = "US0378331005"
Isin.fromString(input) match {
  case Right(isin) =>
    println(s"Parsed ISIN: $isin")
    println(s"  Country code: ${isin.countryCode}")
    println(s"  Security identifier: ${isin.securityIdentifier}")
    println(s"  Check digit: ${isin.checkDigit}")
  case Left(err) =>
    throw new RuntimeException(s"Unable to parse ISIN $input: $err")
}
// Parsed ISIN: US0378331005
//   Country code: US
//   Security identifier: 037833100
//   Check digit: 5
```


## Usage

Add this to your `build.sbt`:

```scala
libraryDependencies += "com.gregorpurdy" %% "ident" % "0.3.0"
```

for the basic identifier data types, or use one or more of the integrations to
support various encodings:

```scala
libraryDependencies += "com.gregorpurdy" %% "ident-circe" % "0.3.0"
libraryDependencies += "com.gregorpurdy" %% "ident-zio-json" % "0.3.0"
libraryDependencies += "com.gregorpurdy" %% "ident-zio-schema" % "0.3.0"
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
