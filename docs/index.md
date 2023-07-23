# Ident

![CI][Badge-CI] [![Sonatype
Releases](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fgregorpurdy%2Fident_2.13%2Fmaven-metadata.xml&label=Sonatype%20Release)](https://repo1.maven.org/maven2/com/gregorpurdy/ident/ident_2.13/)

The `ident` library contains Scala classes for working with identifiers of
various types that have been validated to have the correct format:

* **CIK**: An SEC / EDGAR Central Index Key (CIK) number is a 10-digit numerical
identifier associated with every entity that files with the SEC (both the entity
submitting the filing and the entity that is the subject of the filing).

* **CUSIP**: An [ANSI](https://www.ansi.org) standard (ANSI X9.6-2020) identifier for North American
securities, and select other countries that have adopted it as part of the
[CINS](https://www.cusip.com/identifiers.html#/CINS) system. Defined by the
[Committee on Uniform Security Identification
Procedures](https://www.cusip.com/identifiers.html?section=CUSIP) (CUSIP), from
which it takes its name. The standard is [available for purchase from the ANSI
Store](https://webstore.ansi.org/standards/ascx9/ansix92020).

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

And, it includes integrations with a variety of other modules:

* **[Circe](https://circe.github.io/circe/)**: A
  [JSON](https://www.json.org/json-en.html) library for Scala powered by
  [Cats](https://typelevel.org/cats/). Use the `ident-circe` module.

* **[ZIO Config](https://zio.dev/zio-config/)**: An extension to
  [ZIO](https://zio.dev)'s built-in [Configuration
  facility](https://zio.dev/reference/configuration/). Use the
  `ident-zio-config` module.

* **[ZIO Json](https://zio.dev/zio-json/)**: "A fast and secure
  [JSON](https://www.json.org/json-en.html) library with tight
  [ZIO](https://zio.dev) integration". Use the `ident-zio-json` module.

* **[ZIO Schema](https://zio.dev/zio-schema/)**: "A [ZIO](https://zio.dev)-based
  library for modeling the schema of data structures as first-class values. Use
  the `ident-zio-schema` module.


## Example

```scala mdoc:invisible
import com.gregorpurdy.ident.Isin
```

```scala mdoc
val input = "US0378331005"
Isin.fromString(input) match {
  case Right(isin) =>
    println(s"Parsed ISIN: $isin")
    println(s"  Country code: ${isin.countryCode}")
    println(s"  Security identifier: ${isin.securityIdentifier}")
    println(s"  Check digit: ${isin.checkDigit}")
  case Left(err) =>
    throw new RuntimeException(s"Unable to parse ISIN $input: $err")
}
```


## Usage

Add this to your `build.sbt`:

```scala
libraryDependencies += "com.gregorpurdy" %% "ident" % "@VERSION@"
```

for the basic identifier data types, or use one or more of the integrations to
support various encodings:

```scala
libraryDependencies += "com.gregorpurdy" %% "ident-circe" % "@VERSION@"
libraryDependencies += "com.gregorpurdy" %% "ident-zio-json" % "@VERSION@"
libraryDependencies += "com.gregorpurdy" %% "ident-zio-schema" % "@VERSION@"
```


## JSON

You can use the `isin-circe` or `isin-zio-json` artifacts to get JSON encoders
and decoders for ISINs.

You can also use the `isin-zio-schema` artifact to get generic ZIO Schema
support, which includes support for JSON and other formats as well as other
functionality.


## Tools

The _tools_ directory contains [Scala CLI](https://scala-cli.virtuslab.org/install/) scripts for working with identifiers on the command line:

* _cusip-tool.cs_: Reads CUSIPs from standard input ( one per line) and validates them. Includes a `--fix` mode that writes them back to standard output with corrected check digits.


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
