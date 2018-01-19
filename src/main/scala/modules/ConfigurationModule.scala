package modules

import com.google.inject._
import com.twitter.inject.TwitterModule
import helpers.Configuration

object ConfigurationModule extends TwitterModule {

  @Provides @Singleton
  def provideConfig(): Configuration = Configuration.load("application.conf")
}