# Isin

The `Isin` type is for working with validated International Securities
Identification Numbers (ISINs) as defined in [ISO 6166:2021 Financial services — International securities
identification number (ISIN)](https://www.iso.org/standard/78502.html) ("The Standard").

[The Association of National Numbering Agencies (ANNA)](https://www.anna-web.org/) has [a page
describing ISO 6166](https://www.anna-web.org/standards/isin-iso-6166/).

An ISIN is comprised of 12 ASCII characters with the following parts, in order:

1. A two-letter _Prefix_ in uppercase, designating the issuer's country
of registration or legal domicile, or for OTC derivatives the special code `EZ`. Additional
codes may be allocated by subsequent revisions to The Standard. Country codes follow the
[ISO 3166](https://www.iso.org/iso-3166-country-codes.html) standard.
2. A nine-character uppercase alphanumeric _Basic Code_ assigned by the corresponding
National Numbering Agency, zero-padded on the left if the underlying code is shorter than nine
characters.
3. A single decimal digit representing the _Check Digit_ computed using what the standard calls
the "modulus 10 'double-add-double' check digit".

Use `Isin.from_string()` to convert a string to a validated Isin.
