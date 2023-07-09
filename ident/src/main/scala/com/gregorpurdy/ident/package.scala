package com.gregorpurdy

package object ident {

  private[ident] def normalize(s: String) = s.replaceAll("""(\h|\v)+""", " ").trim.toUpperCase

}
