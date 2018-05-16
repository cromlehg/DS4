package models

class AccountStatus(val id: Long, val status: String) {

  override def hashCode = 17 * status.hashCode

  override def equals(obj: Any) = obj match {
    case s: AccountStatus => s.status == status
    case _                => false
  }

  override def toString = status

}

object AccountStatus {

  val ACTIVE = "active"

  val LOCKED = "locked"

  def apply(id: Long, status: String): AccountStatus =
    new AccountStatus(id, status)

}
