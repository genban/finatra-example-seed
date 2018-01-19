package models

case class UserInfo(username: String, headimgurl: String)

object UserInfo {

  def default = UserInfo("username", "headimgurl")
}
