# ISIN

![CI][Badge-CI]

A Scala class for working with validated International Security Identifiers
(ISINs) as defined in [ISO 6166:2021 Financial services — International
securities identification number
(ISIN)](https://www.iso.org/standard/78502.html) ("The Standard").

This library is part of the Financial Identifiers series:

* [ISIN](https://github.com/gnp/isin-sc/): International Securities Identification Number (ISO 6166:2021)


## Usage

Add this to your `build.sbt`:

```scala
libraryDependencies += "com.gregorpurdy.ident" %% "isin" % "0.1.0-SNAPSHOT"
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
