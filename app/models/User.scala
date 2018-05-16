package models

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.tz.DateTimeZoneBuilder
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale

//object User {
//
//  def apply(id: Long,
//            email: String,
//            hash: Option[String],
//            session: Option[Session],
//            roles: List[String],
//            avatar: Option[models.Media],
//            userStatus: UserStatus,
//            accountStatus: AccountStatus,
//            name: Option[String],
//            surname: Option[String],
//            timezone: models.Timezone,
//            registered: Long): User =
//    new User(id,
//      email,
//      hash,
//      session,
//      roles,
//      avatar,
//      userStatus,
//      accountStatus,
//      name,
//      surname,
//      timezone,
//      registered)
//
//}

class User(val id: Long,
           val email: String,
           val hash: Option[String],
           val session: Option[Session],
           val roles: List[String],
           val avatar: Option[models.Media],
           val userStatus: UserStatus,
           val accountStatus: AccountStatus,
           val name: Option[String],
           val surname: Option[String],
           val timezone: models.Timezone,
           val registered: Long) {

  val ldt = new LocalDateTime(registered * 1000L, DateTimeZone.UTC)

  override def equals(obj: Any) = obj match {
    case user: User => user.email == email
    case _          => false
  }

  override def toString = email

  def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

  def getRegistered: LocalDateTime = ldt

}
