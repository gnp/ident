package com.gregorpurdy.ident

import zio.schema.Schema

object ISINZIOSchema {

  implicit val isinZioSchema: Schema[ISIN] =
    Schema.primitive[String].transformOrFail(string => ISIN.parse(string), isin => Right(isin.value))

}
