/*
 * Copyright 2023 Gregor Purdy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gregorpurdy.ident

import java.time.Instant
import scala.util.matching.Regex

/** Legal Entity Identifier (LEI) is (according to the GLEIF web site):
  *
  * <q>&hellip; a 20-character, alpha-numeric code based on the ISO 17442 standard developed by the International
  * Organization for Standardization (ISO). It connects to key reference information that enables clear and unique
  * identification of legal entities participating in financial transactions. Each LEI contains information about an
  * entity’s ownership structure and thus answers the questions of 'who is who’ and ‘who owns whom’. Simply put, the
  * publicly available LEI data pool can be regarded as a global directory, which greatly enhances transparency in the
  * global marketplace. </q>
  *
  * @see
  *   ISO 17442-1:2020(E) "Financial services — Legal entitty identifier (LEI) — Part 1: Assignment"
  *
  * @see
  *   https://www.gleif.org/en/about-lei/introducing-the-legal-entity-identifier-lei
  * @see
  *   https://www.gleif.org/en/about-lei/iso-17442-the-lei-code-structure
  * @see
  *   ISO 17442-1:2020 "Financial services — Legal entity identifier (LEI) — Part 1: Assignment"
  *   (https://www.iso.org/standard/78829.html)
  */
sealed case class Lei(value: String) {

  /** @return true if the LEI does not conform to the standard (with expected check digits), or false otherwise */
  def isConforming: Boolean = checkDigits == "%02d".format(Lei.compute_check_digits(value.substring(0, 18) + "00"))

  /** "Prefix used to ensure the uniqueness among codes from LEI issuers (Local Operating Units or LOUs)."
    *
    * @see
    *   https://www.gleif.org/en/about-lei/iso-17442-the-lei-code-structure
    */
  def louIdentifier: String = value.substring(0, 4)

  /** "Entity-specific part of the code generated and assigned by LOUs according to transparent, sound and robust
    * allocation policies. As required by ISO 17442, it contains no embedded intelligence."
    *
    * @see
    *   https://www.gleif.org/en/about-lei/iso-17442-the-lei-code-structure
    */
  def entityIdentifier: String = value.substring(4, 18)

  /** "Two check digits as described in the ISO 17442 standard"
    *
    * @see
    *   https://www.gleif.org/en/about-lei/iso-17442-the-lei-code-structure
    */
  def checkDigits: String = value.substring(18, 20)

  override def toString: String = value

  def toStringTagged: String = s"lei:$value"

  def unapply: Option[String] = Some(value)

}

object Lei {

  implicit val ord: Ordering[Lei] = Ordering.by(_.value)

  val leiFormat: Regex = "([A-Z0-9]{4})([A-Z0-9]{14})([0-9]{2})".r
  val louIdentifierFormat: String = "([A-Z0-9]{4})"
  val entityIdentifierFormat: String = "([A-Z0-9]{14})"
  val checkDigitsFormat: String = "([0-9]{2})"

  /** This algorithm uses as MAX value computed from Long.MaxValue by subtracting the numeric value of 'Z' (35) and
    * dividing by 100", so we can use MAX as a threshold to compute the modulus before we are done, but never as long as
    * there is still room to multiply by 100 and add 36 without exceeding Long.MaxValue
    */
  private val MAX = (Long.MaxValue - Character.getNumericValue('Z')) / 100

  private val MODULUS = 97

  /** @param string
    *   you can call this with a complete identifier with check digits already appended, and check that the result is 1
    *   as part of verifying that the identifier is valid. Be sure the input string is purely alphanumeric.
    */
  def compute_iso7064_mod97_10(string: String): Int = {
    val temp = string.map(Character.getNumericValue(_).toLong).fold(0L) { (total, charValue) =>
      val factor = if (charValue > 9) 100 else 10
      val nextValue = total * factor + charValue
      if (nextValue > MAX) nextValue % MODULUS else nextValue
    }

    (temp % MODULUS).toInt
  }

  /** @param string
    *   should be a string WITH two zero check digits ("00") already appended to the right to calculate the correct
    *   check digits for a base identifier. Be sure the input string is purely alphanumeric.
    */
  def compute_check_digits(string: String): Int = {
    98 - compute_iso7064_mod97_10(string)
  }

  /** Strict validation. Exactly 20 characters, all letters already uppercase, no non-alphanumeric characters, and check
    * digits are correct.
    *
    * Does _not_ consider whitelisted non-conforming LEIs to be valid.
    */
  def validate(value: String): Boolean = {
    if (value.length != 20) false
    else if (value.substring(0, 18).find(c => (c < 'A' || c > 'Z') && (c < '0' || c > '9')).isDefined) false
    else {
      val check = value.substring(18, 20)
      if (check.find(c => (c < '0' || c > '9')).isDefined) false
      else {
        val n = check.toInt
        if (n < 2 || n > 98) false
        else 1 == compute_iso7064_mod97_10(value)
      }
    }
  }

  /** Non-strict validation. Either a whitelisted non-conforming LEI, or exactly 20 characters, all letters already
    * uppercase, no non-alphanumeric characters, and check digits are correct.
    */
  def validateAllowNonConforming(value: String): Boolean =
    if (Whitelist.contains(value)) true else validate(value)

  /** Internal method after louIdentifier, entityIdentifier and checkDigits have already been validated to be the right
    * length and the right character set.
    *
    * TODO: Should we check for louIdentifer + entityIdentifier in whitelist, even with different checkDigits, then...
    * (?)
    */
  private def fromPartsUnsafe(
      louIdentifier: String,
      entityIdentifier: String,
      checkDigits: String
  ): Either[String, Lei] = {
    val temp = s"$louIdentifier$entityIdentifier$checkDigits"

    if (Whitelist.contains(temp)) {
      Right(new Lei(temp))
    } else {
      val checkDigitsNumeric = checkDigits.toInt

      if (checkDigitsNumeric < 2 || checkDigitsNumeric > 98)
        Left(s"checkDigits '$checkDigits' are not in the range [02-98]")
      else {
        val realCheck = compute_check_digits(louIdentifier + entityIdentifier + "00")

        if (checkDigitsNumeric != realCheck)
          Left(f"Check digits '$checkDigits' do not match correct check digits '${realCheck}%02d'")
        else
          Right(new Lei(temp))
      }
    }
  }

  def fromParts(louIdentifier: String, entityIdentifier: String, checkDigits: String): Either[String, Lei] = {
    val tempLouIdentifier = normalize(louIdentifier)
    val tempEntityIdentifier = normalize(entityIdentifier)
    val tempCheckDigits = normalize(checkDigits)

    if (!tempLouIdentifier.matches(louIdentifierFormat))
      Left(s"louIdentifier '$louIdentifier' is not exactly 4 alphanumeric characters")
    else if (!tempEntityIdentifier.matches(entityIdentifierFormat))
      Left(s"entityIdentifier '$entityIdentifier' is not exactly 14 alphanumeric characters")
    else if (!tempCheckDigits.matches(checkDigitsFormat))
      Left(s"checkDigits '$checkDigits' are not exactly two decimal digits")
    else
      fromPartsUnsafe(tempLouIdentifier, tempEntityIdentifier, tempCheckDigits)
  }

  def fromPartsCalcCheckDigits(louIdentifier: String, entityIdentifier: String): Either[String, Lei] = {
    val tempLouIdentifier = normalize(louIdentifier)
    val tempEntityIdentifier = normalize(entityIdentifier)

    if (!tempLouIdentifier.matches(louIdentifierFormat))
      Left(s"louIdentifier '$louIdentifier' does not match required format")
    else if (!tempEntityIdentifier.matches(entityIdentifierFormat))
      Left(s"entityIdentifier '$entityIdentifier' does not match required format")
    else {
      val realCheck = "%02d".format(compute_check_digits(tempLouIdentifier + tempEntityIdentifier + "00"))
      Right(new Lei(s"$tempLouIdentifier$tempEntityIdentifier$realCheck"))
    }
  }

  def fromString(value: String): Either[String, Lei] =
    normalize(value) match {
      case leiFormat(lou, entity, check) =>
        fromPartsUnsafe(lou, entity, check)
      case _ =>
        Left(s"Value is not exactly 18 alpha-numeric characters plus 2 decimal check digits: '$value'")
    }

  /** Some LEIs have been issued with erroneous check digits according to the specification.
    *
    * This reference list of 124 technically non-conforming but permissible LEIs was obtained from Data Quality Feedback
    * <dataqualityfeedback@gleif.org> on 2021-02-18 via email with subject "Re: [Ticket#2021021110000542] Data quality
    * issue in ISIN-to-LEI relationship file" in response to an inquiry I made on 2021-02-11 via email subject "Data
    * quality issue in ISIN-to-LEI relationship file".
    *
    * The originally received CSV file, is incorporated into this module as a resource in package
    * "com.gregorpurdy.ident" with name "2021-02-18_valid-LEI-with-incorrect-check-digits.csv"
    */
  object Whitelist {

    sealed trait RegistrationStatus
    object RegistrationStatus {
      case object Issued extends RegistrationStatus
      case object Lapsed extends RegistrationStatus
      case object Merged extends RegistrationStatus
      case object Retired extends RegistrationStatus
    }

    import RegistrationStatus.*

    final case class Entry(index: Int, lei: String, timestamp: Instant, registrationStatus: RegistrationStatus)

    /** The Array of whitelist Entries.
      *
      * Note: Accessing this value implicitly initializes the whitelist data structures, if they have not already been
      * initialized.
      */
    lazy val entries: Array[Entry] = Array(
      Entry(0, "315700X8JQ3IU0NGK501", Instant.parse("2013-07-20T00:00:00+02:00"), Issued),
      Entry(1, "31570010000000006400", Instant.parse("2013-12-30T00:00:00.000+01:00"), Issued),
      Entry(2, "31570010000000019301", Instant.parse("2014-01-06T00:00:00.000+01:00"), Issued),
      Entry(3, "31570010000000009601", Instant.parse("2014-01-07T00:00:00.000+01:00"), Issued),
      Entry(4, "31570010000000025800", Instant.parse("2014-01-10T00:00:00.000+01:00"), Lapsed),
      Entry(5, "31570010000000029001", Instant.parse("2014-01-13T00:00:00.000+01:00"), Issued),
      Entry(6, "31570010000000035500", Instant.parse("2014-01-13T00:00:00.000+01:00"), Lapsed),
      Entry(7, "31570010000000038701", Instant.parse("2014-01-15T00:00:00.000+01:00"), Merged),
      Entry(8, "31570010000000045200", Instant.parse("2014-01-16T00:00:00.000+01:00"), Issued),
      Entry(9, "31570010000000048401", Instant.parse("2014-01-16T00:00:00.000+01:00"), Issued),
      Entry(10, "31570010000000054900", Instant.parse("2014-01-16T00:00:00.000+01:00"), Issued),
      Entry(11, "31570010000000064600", Instant.parse("2014-01-20T00:00:00.000+01:00"), Issued),
      Entry(12, "31570010000000067801", Instant.parse("2014-01-21T00:00:00.000+01:00"), Issued),
      Entry(13, "31570010000000077501", Instant.parse("2014-01-21T00:00:00.000+01:00"), Issued),
      Entry(14, "31570010000000103400", Instant.parse("2014-01-23T00:00:00.000+01:00"), Issued),
      Entry(15, "31570010000000116301", Instant.parse("2014-01-23T00:00:00.000+01:00"), Issued),
      Entry(16, "31570010000000084000", Instant.parse("2014-01-23T00:00:00.000+01:00"), Lapsed),
      Entry(17, "31570010000000087201", Instant.parse("2014-01-24T00:00:00.000+01:00"), Lapsed),
      Entry(18, "31570010000000096901", Instant.parse("2014-01-24T00:00:00.000+01:00"), Issued),
      Entry(19, "31570010000000106601", Instant.parse("2014-01-24T00:00:00.000+01:00"), Issued),
      Entry(20, "31570010000000122800", Instant.parse("2014-01-28T00:00:00.000+01:00"), Issued),
      Entry(21, "31570010000000126001", Instant.parse("2014-01-28T00:00:00.000+01:00"), Issued),
      Entry(22, "31570020000000005900", Instant.parse("2014-01-28T00:00:00.000+01:00"), Issued),
      Entry(23, "3157006I3B6RSTPQLI00", Instant.parse("2014-01-29T00:00:00.000+01:00"), Issued),
      Entry(24, "315700M5843O6DU83901", Instant.parse("2014-01-29T00:00:00.000+01:00"), Issued),
      Entry(25, "3157008ZY7CW6LVU5J00", Instant.parse("2014-01-30T00:00:00.000+01:00"), Issued),
      Entry(26, "3157008KD17KROO7UT01", Instant.parse("2014-01-30T00:00:00.000Z"), Issued),
      Entry(27, "315700D23JL5C1DZNT00", Instant.parse("2014-01-31T00:00:00.000+01:00"), Issued),
      Entry(28, "315700TF5Z7T28HZJK01", Instant.parse("2014-01-31T00:00:00.000+01:00"), Lapsed),
      Entry(29, "3157008VKYORMUNC5O01", Instant.parse("2014-02-03T00:00:00.000+01:00"), Lapsed),
      Entry(30, "315700VMAJZ9JZTXNQ00", Instant.parse("2014-02-03T00:00:00.000+01:00"), Issued),
      Entry(31, "3157002CKDQOCIHE5H01", Instant.parse("2014-02-03T00:00:00.000+01:00"), Issued),
      Entry(32, "315700JXUHL9H2C3P700", Instant.parse("2014-02-03T00:00:00.000+01:00"), Issued),
      Entry(33, "315700WH3YMKHCVYW201", Instant.parse("2014-02-04T00:00:00.000+01:00"), Issued),
      Entry(34, "315700Q1S8O1UORF9700", Instant.parse("2014-02-04T00:00:00.000+01:00"), Issued),
      Entry(35, "3157005P1M669LB5QO01", Instant.parse("2014-02-05T00:00:00.000+01:00"), Merged),
      Entry(36, "315700WYOZ6994UATN00", Instant.parse("2014-02-06T00:00:00.000+01:00"), Issued),
      Entry(37, "315700VG7PTE9EJJRX01", Instant.parse("2014-02-07T00:00:00.000+01:00"), Issued),
      Entry(38, "315700DJ07P6OX10FK01", Instant.parse("2014-02-07T00:00:00.000+01:00"), Retired),
      Entry(39, "315700JKZH6I0067ND01", Instant.parse("2014-02-07T00:00:00.000+01:00"), Lapsed),
      Entry(40, "315700P6TZOLP92KN801", Instant.parse("2014-02-10T00:00:00.000+01:00"), Issued),
      Entry(41, "315700UYFD5GF9R13F01", Instant.parse("2014-02-10T00:00:00.000+01:00"), Issued),
      Entry(42, "315700GXBGM8DBYKHF01", Instant.parse("2014-02-11T00:00:00.000+01:00"), Issued),
      Entry(43, "315700S3TF79ALV82F01", Instant.parse("2014-02-11T00:00:00.000+01:00"), Issued),
      Entry(44, "315700JO5E28SRE00Q01", Instant.parse("2014-02-11T00:00:00.000+01:00"), Issued),
      Entry(45, "315700PXKOSX7WQV4N00", Instant.parse("2014-02-11T00:00:00.000+01:00"), Issued),
      Entry(46, "315700HZU4SMI8LZTU00", Instant.parse("2014-02-12T00:00:00.000+01:00"), Issued),
      Entry(47, "315700Y1W7W1JHAUBW00", Instant.parse("2014-02-12T00:00:00.000+01:00"), Issued),
      Entry(48, "315700S22RGYRIEEOT00", Instant.parse("2014-02-13T00:00:00.000+01:00"), Issued),
      Entry(49, "315700TCC9NTEP7J8Z01", Instant.parse("2014-02-13T00:00:00Z"), Issued),
      Entry(50, "315700DKCD4QSKLAMO01", Instant.parse("2014-02-14T00:00:00.000+01:00"), Lapsed),
      Entry(51, "31570067WSCDST0S3F01", Instant.parse("2014-02-14T00:00:00.000+01:00"), Issued),
      Entry(52, "315700MBYPT6PGKO7M01", Instant.parse("2014-02-14T00:00:00.000+01:00"), Issued),
      Entry(53, "315700G5G24XYL1TXH00", Instant.parse("2014-02-17T00:00:00.000+01:00"), Issued),
      Entry(54, "315700WZPEIS41QDKE00", Instant.parse("2014-02-17T00:00:00.000+01:00"), Issued),
      Entry(55, "315700N0VEIBHP0NPQ01", Instant.parse("2014-02-18T00:00:00.000+01:00"), Issued),
      Entry(56, "315700ET7M7VQ4C84R00", Instant.parse("2014-02-20T00:00:00.000+01:00"), Issued),
      Entry(57, "315700MW2F0KFR45QW01", Instant.parse("2014-02-20T00:00:00.000+01:00"), Issued),
      Entry(58, "315700P89WR82VNB8Z00", Instant.parse("2014-02-20T00:00:00.000+01:00"), Issued),
      Entry(59, "315700P40OV6BT045900", Instant.parse("2014-02-24T00:00:00.000+01:00"), Lapsed),
      Entry(60, "315700RTEHY362KXWJ00", Instant.parse("2014-02-26T00:00:00.000+01:00"), Issued),
      Entry(61, "315700TWGZ89LLSRS000", Instant.parse("2014-02-27T00:00:00.000+01:00"), Lapsed),
      Entry(62, "315700XE21UYOA3GAC01", Instant.parse("2014-02-27T00:00:00.000+01:00"), Issued),
      Entry(63, "3157009OVCV07O4HXM00", Instant.parse("2014-02-28T00:00:00.000+01:00"), Issued),
      Entry(64, "315700PLI0I7W8IOV400", Instant.parse("2014-03-03T00:00:00.000+01:00"), Issued),
      Entry(65, "315700ADOXDR5PCY5400", Instant.parse("2014-03-03T00:00:00.000+01:00"), Lapsed),
      Entry(66, "315700PN3J57ZUNF1V00", Instant.parse("2014-03-03T00:00:00.000+01:00"), Issued),
      Entry(67, "3157001TPR6K4GBTLN00", Instant.parse("2014-03-03T00:00:00.000+01:00"), Issued),
      Entry(68, "315700K7NYVSQJNTN401", Instant.parse("2014-03-04T00:00:00.000+01:00"), Merged),
      Entry(69, "315700LDDN3RM7Y2MP00", Instant.parse("2014-03-07T00:00:00.000+01:00"), Lapsed),
      Entry(70, "315700O666JVNCQU9X00", Instant.parse("2014-03-07T00:00:00.000+01:00"), Lapsed),
      Entry(71, "3157009FTHFDDK7FHW01", Instant.parse("2014-03-12T00:00:00.000+01:00"), Lapsed),
      Entry(72, "315700BM9Z39TNTGQW00", Instant.parse("2014-03-13T00:00:00.000+01:00"), Issued),
      Entry(73, "3157006FR3JBBOLOMX01", Instant.parse("2014-03-14T00:00:00.000+01:00"), Lapsed),
      Entry(74, "315700QGM4XWZE1I5N01", Instant.parse("2014-03-20T00:00:00.000+01:00"), Issued),
      Entry(75, "315700OASRCM664PAW01", Instant.parse("2014-03-21T00:00:00.000+01:00"), Issued),
      Entry(76, "315700T2EEQAPBO0C301", Instant.parse("2014-03-25T00:00:00.000+01:00"), Issued),
      Entry(77, "315700EIYO2TLSEGQ700", Instant.parse("2014-03-26T00:00:00.000+01:00"), Retired),
      Entry(78, "315700YS6RQ5TF3VBP01", Instant.parse("2014-03-27T00:00:00.000+01:00"), Issued),
      Entry(79, "3157001K2LAL04D87901", Instant.parse("2014-04-07T00:00:00.000+01:00"), Merged),
      Entry(80, "31570058O0Z320C4GZ00", Instant.parse("2014-04-09T00:00:00.000+01:00"), Merged),
      Entry(81, "315700X40GNCOUWJYR00", Instant.parse("2014-04-17T00:00:00.000+01:00"), Issued),
      Entry(82, "315700I3W2AFHP8MNQ01", Instant.parse("2014-04-22T00:00:00.000+01:00"), Issued),
      Entry(83, "315700XI4Z8GF5BDUJ01", Instant.parse("2014-04-23T00:00:00.000+01:00"), Lapsed),
      Entry(84, "315700PGTT5GWZRJG000", Instant.parse("2014-05-07T00:00:00.000+01:00"), Merged),
      Entry(85, "315700NNMGS8F3P2CN00", Instant.parse("2014-06-05T00:00:00.000+01:00"), Issued),
      Entry(86, "3157006B6JVZ5DFMSN00", Instant.parse("2014-06-25T00:00:00.000+01:00"), Lapsed),
      Entry(87, "315700OVW93X0T3HP200", Instant.parse("2014-06-25T00:00:00.000+01:00"), Issued),
      Entry(88, "315700B8401GTFFY6X01", Instant.parse("2014-06-27T00:00:00.000+01:00"), Issued),
      Entry(89, "315700RK8M4FAHMYAP01", Instant.parse("2014-07-04T00:00:00.000+01:00"), Issued),
      Entry(90, "315700EZSEA51937KX01", Instant.parse("2014-07-08T00:00:00.000+01:00"), Issued),
      Entry(91, "315700XSCP1S8WOD8E01", Instant.parse("2014-07-30T00:00:00.000+01:00"), Issued),
      Entry(92, "315700BZ5F7DRYG2UM00", Instant.parse("2014-08-07T00:00:00.000+01:00"), Issued),
      Entry(93, "315700Y5JNQMMUF5ID01", Instant.parse("2014-08-07T00:00:00.000+01:00"), Issued),
      Entry(94, "315700P4N9VSLK5QZV01", Instant.parse("2014-09-05T00:00:00.000+01:00"), Lapsed),
      Entry(95, "3157005WT1SENAE17R00", Instant.parse("2014-09-23T00:00:00.000+01:00"), Issued),
      Entry(96, "315700659AALVVLVIO01", Instant.parse("2014-10-08T00:00:00.000+01:00"), Merged),
      Entry(97, "3157006KT1EZ15OIXW00", Instant.parse("2014-11-03T00:00:00.000+01:00"), Lapsed),
      Entry(98, "3157005VJE7A3MBUS201", Instant.parse("2014-11-03T00:00:00.000+01:00"), Issued),
      Entry(99, "315700JICZ3SY5SAXX00", Instant.parse("2014-11-18T00:00:00.000+01:00"), Issued),
      Entry(100, "315700UKZXWXEO126601", Instant.parse("2014-12-03T00:00:00.000+01:00"), Issued),
      Entry(101, "3157004PTVTDOKB46401", Instant.parse("2014-12-19T00:00:00.000+01:00"), Issued),
      Entry(102, "31570029808HJVCNFA01", Instant.parse("2014-12-30T00:00:00.000+01:00"), Merged),
      Entry(103, "3157006DE3SPNIUY9K01", Instant.parse("2015-01-16T00:00:00.000+01:00"), Lapsed),
      Entry(104, "315700TXNX10N8XH4K00", Instant.parse("2015-01-20T00:00:00.000+01:00"), Lapsed),
      Entry(105, "3157001KM8GOU7PXZY01", Instant.parse("2015-03-02T00:00:00.000+01:00"), Issued),
      Entry(106, "315700XEFYMA5EZ0P500", Instant.parse("2015-03-06T00:00:00.000+01:00"), Lapsed),
      Entry(107, "3157004R6CH6C1P4KX00", Instant.parse("2015-04-20T00:00:00.000+01:00"), Issued),
      Entry(108, "315700LWYOZNQ7V1T100", Instant.parse("2015-04-29T00:00:00.000+01:00"), Issued),
      Entry(109, "315700T6T49EDM16YO01", Instant.parse("2015-05-07T00:00:00.000+01:00"), Issued),
      Entry(110, "315700A0UB9Q7DOQIZ00", Instant.parse("2015-05-11T00:00:00.000+01:00"), Issued),
      Entry(111, "315700HS7WJ1B0SUWM01", Instant.parse("2015-05-14T00:00:00.000+01:00"), Issued),
      Entry(112, "315700GZA843JXKTJ400", Instant.parse("2015-05-29T00:00:00.000+01:00"), Issued),
      Entry(113, "315700HXOEOK58E58P01", Instant.parse("2015-06-05T00:00:00.000+01:00"), Issued),
      Entry(114, "3157005RUI28M8FANK00", Instant.parse("2015-06-23T00:00:00.000+01:00"), Issued),
      Entry(115, "3157003FQSSGS9OZ9E01", Instant.parse("2015-07-02T00:00:00.000+01:00"), Lapsed),
      Entry(116, "315700VITYR7AL4M9S01", Instant.parse("2015-07-08T00:00:00.000+01:00"), Issued),
      Entry(117, "315700T8U7IU4W8J3A01", Instant.parse("2015-08-06T00:00:00Z"), Lapsed),
      Entry(118, "3157001MLDD3SDFQA901", Instant.parse("2015-08-26T00:00:00.000+01:00"), Lapsed),
      Entry(119, "315700OFW4YCOBNX4U01", Instant.parse("2015-09-14T00:00:00.000+01:00"), Lapsed),
      Entry(120, "315700WR4IHOO1M5LP00", Instant.parse("2015-10-08T00:00:00.000+01:00"), Lapsed),
      Entry(121, "315700UJ6N4LGKLNPB00", Instant.parse("2015-10-13T00:00:00.000+01:00"), Issued),
      Entry(122, "315700BBRQHDWX6SHZ00", Instant.parse("2015-10-26T00:00:00Z"), Issued),
      Entry(123, "3157000VAJWZ3P8ZED00", Instant.parse("2015-11-09T00:00:00.000+01:00"), Issued)
    )

    /** The Map of non-conforming LEI String keys to whitelist Entries. The keys contain only uppercase alphanumeric
      * characters.
      *
      * Note: Accessing this value implicitly initializes the whitelist data structures, if they have not already been
      * initialized.
      */
    lazy val lookup: Map[String, Entry] = Map(entries.map(e => e.lei -> e).toIndexedSeq: _*)

    /** Determine whether the provided value is present in the whitelist of non-conforming LEIs. Be sure the value
      * provided is already normalized (no spaces, only uppercase alphanumeric characters).
      *
      * Note: Calling this method implicitly initializes the whitelist data structures, if they have not already been
      * initialized.
      *
      * @return
      *   true if `value` is present in the whitelist, false otherwise.
      */
    def contains(value: String) = lookup.contains(value)

    /** Force initialization of the lazy Whitelist data structures.
      *
      * Note: This is not necessary for correct operation. Its only purpose is to give you the option to pre-initialize
      * the data structures at an earlier time of your choosing than your first use of the whitelist.
      */
    def initialize(): Unit = { lookup.size; () }

  }

}
