# Cusip

The `Cusip` type is for working with validated Committee on Uniform Security
Identification Procedures (CUSIP) identifiers as defined in [ANSI X9.6-2020
Financial Services - Committee on Uniform Security Identification Procedures
Securities Identification
CUSIP](https://webstore.ansi.org/standards/ascx9/ansix92020) ("The Standard").

[CUSIP Global Services (CGS)](https://www.cusip.com/) has [a page describing
CUSIP identifiers](https://www.cusip.com/identifiers.html).

A CUSIP "number" (so-called by The Standard because originally they were
composed only of decimal digits, but now they can also use letters) is comprised
of 9 ASCII characters with the following parts, in order (Section 3.1 "CUSIP
number length" of the standard):

1. A six-character uppercase alphanumeric _Issuer Number_.
2. A two-character uppercase alphanumeric _Issue Number_.
3. A single decimal digit representing the _Check Digit_ computed using what The
Standard calls the "modulus 10 'double-add-double' technique".

Note: The Standard does not specify uppercase for the alphabetic characters but
uniformly presents examples only using uppercase. Therefore this implementation
forces to uppercase.

Although The Standard asserts that CUSIP numbers are not assigned using
alphabetic 'I' and 'O' nor using digits '1' and '0' to avoid confusion, digits
'1' and '0' are common in current real-world CUSIP numbers. A survey of a large
set of values turned up none using letter 'I' or letter 'O', so it is plausible
that 'I' and 'O' are indeed not used. In any case, `Cusip` does _not_ treat any
of these four character values as invalid.

CUSIP number "issuance and dissemination" are managed by [CUSIP Global Services
(CGS)](https://www.cusip.com/) per section B.1 "Registration Authority" of The
Standard. In addition, there are provisions for privately assigned identifiers
(see below).


## Usage

Use `Cusip.fromString()` to convert a string to a validated CUSIP:

```scala mdoc
Cusip.fromString("09739D100") match {
  case Left(error) => { /* ... */ }
  case Right(cusip) => { /* ... */ }
}
```

If you just want to check if a string value is in a valid CUSIP format (with the
correct _Check Digit_), use `isValid()` (which pre-normalizes the input before
checking) or `isValidStrict()` (which requires an already normalized String as
input).


## CUSIP

Since its adoption in 1968, CUSIP has been the standard security identifier for:

* United States of America
* Canada
* Bermuda
* Cayman Islands
* British Virgin Islands
* Jamaica

Since the introduction of the ISIN standard ([ISO
6166](https://www.iso.org/standard/78502.html)), CUSIP has been adopted as the
ISIN _Security Identifier_ for many more territories in the creation of ISIN
identifiers.


## Private use

The CUSIP code space has allocations for both private _Issuer Numbers_ and
private _Issue Numbers_.

You can determine whether or not a CUSIP is intended for private use by using
the `isPrivateUse` method. A private use CUSIP is one that either
`hasPrivateIssuer` or `isPrivateIssue`. The has/is distinction is because a
CUSIP represents ("is") an Issue (Security) offered by an "Issuer" (the Security
"has" an Issuer).


### Private Issue Numbers

In Section 3.2 "Issuer Number" of The Standard, "privately assigned identifiers"
are defined as those having _Issuer Number_ ending in "990" through "999".

In Section C.8.1.3 "Issuer Numbers Reserved for Internal Use" of the Standard,
expands that set with the following additional _Issuer Numbers_:

* those ending in "99A" through "99Z"
* those from "990000" through "999999"
* those from "99000A" through "99999Z"

Such CUSIPs are reserved for this use only, and will not be assigned by the
Registration Authority.

You can use the `hasPrivateIssuer` method to detect this case.

Note that The Standard says that in all cases a "Z" in the "5th and 6th position
has been reserved for use by the Canadian Depository for Securities." There are
no examples given, and it is not clear whether this means literally "and"
("0000ZZ005" would be reserved but "0000Z0002" and "00000Z003" would not) or if
it actually means "and/or" (all of "0000ZZ005", "0000Z0002" and "00000Z003"
would be reserved). Because this is not clear from the text of the standard,
this rule is not represented here.


### Private Issuer Numbers

In Section C.8.2.6 "Issue Numbers Reserved for Internal Use", The Standard
specifies that _Issue Numbers_ "90" through "99" and "9A" through "9Y" are
reserved for private use, potentially in combination with non-private-use
_Issuer Numbers_.


## CUSIP International Numbering System (CINS)

While the primary motivation for the creation of the CUSIP standard was
representation of U.S. and Canadian securities, it was extended in 1989 for
non-North American issues through definition of a CUSIP International Numbering
System (CINS). On 1991-01-01 CINS became the only allowed way of issuing CUSIP
identifiers for non-North American securities.

A CUSIP with a letter in the first position is a CINS number, and that letter
identifies the country or geographic region of the _Issuer_.

Use the `isCins()` method to discriminate between CINS and conventional CUSIPs,
and the `cinsCountryCode` method to extract the CINS Country Code as an
`Option[Char]`.

The country codes are:

|code|region        |code|region     |code|region       |code|region         |
|----|--------------|----|-----------|----|-------------|----|---------------|
|`A` |Austria       |`H` |Switzerland|`O` |(Unused)     |`V` |Africa - Other |
|`B` |Belgium       |`I` |(Unused)   |`P` |South America|`W` |Sweden         |
|`C` |Canada        |`J` |Japan      |`Q` |Australia    |`X` |Europe - Other |
|`D` |Germany       |`K` |Denmark    |`R` |Norway       |`Y` |Asia           |
|`E` |Spain         |`L` |Luxembourg |`S` |South Africa |`Z` |(Unused)       |
|`F` |France        |`M` |Mid-East   |`T` |Italy        |    |               |
|`G` |United Kingdom|`N` |Netherlands|`U` |United States|    |               |

Even though country codes `I`, `O` and `Z` are unused, this class reports CUSIPs
starting with those letters as being in the CINS format via `isCins()` and
returns them via `cinsCountryCode()` because The Standard says CINS numbers are
those CUSIPs starting with a letter. If you care about the distinction between
the two, use `isCinsBase()` and `isCinsExtended()`.

See section C.7.2 "Non-North American Issues -- CUSIP International Numbering
System" of The Standard.


## Private Placement Number (PPN)

According to Section C.7.2 "Private Placements" of The Standard, The Standard
defines three non-alphanumeric character values to support a special use for the
"PPN System". They are '`*`' (value 36), '`@`' (value 37) and '`#`' (value 38)
(see section A.3 "Treatment of Alphabetic Characters").

CUSIPs using these extended characters are not supported by this class because
the extended characters are not supported by ISINs, and CUSIPs are incorporated
as the _Security Identifier_ for ISINs for certain _Country Codes_.
