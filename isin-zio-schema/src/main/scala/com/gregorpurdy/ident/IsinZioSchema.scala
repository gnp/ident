package com.gregorpurdy.ident

import zio.schema.Schema

object IsinZioSchema {

  implicit val isinSchema: Schema[ISIN] =
    Schema.primitive[String].transformOrFail(string => ISIN.parse(string), isin => Right(isin.value))

}
