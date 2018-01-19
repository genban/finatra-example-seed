//package persistence.quill
//
//import io.getquill.context.async._
//
///**
// * @author Corbin
// */
//trait JsValueEncoding { //FIXME: JsValue dependencies
//  this: AsyncContext[_, _, _] =>
//
//  implicit val jsValueEncoder: Encoder[JsValue] = encoder[JsValue]({
//    json: JsValue => Json.stringify(json)
//  }, SqlTypes.VARCHAR)
//
//  implicit val jsValueDecoder: Decoder[JsValue] = decoder[JsValue]({
//    case input: String => Json.parse(input)
//  }, SqlTypes.VARCHAR)
//}
