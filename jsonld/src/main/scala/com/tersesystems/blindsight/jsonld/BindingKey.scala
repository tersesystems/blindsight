package com.tersesystems.blindsight.jsonld

/**
 * A binding key consists of a name and a binding operation
 * defined by the instance.
 */
trait BindingKey {
  def name: String
}

/**
 * A binding key that returns a [[ValueBinding]] using `bindValue`.
 */
trait ValueBindingKey extends BindingKey {
  def bindValue[T: ValueMapper]: ValueBinding[T] = ValueBinding(this)

  def bindValues[T: ValueMapper]: ValueArrayBinding[T] = ValueArrayBinding(this)
}

/**
 * A binding key that returns a [[NodeObjectBinding]] using `bindObject`.
 */
trait NodeObjectBindingKey extends BindingKey {
  def bindObject[T: NodeObjectMapper] = new NodeObjectBinding[T](this)

  def bindObjects[T: NodeObjectMapper] = new NodeObjectArrayBinding[T](this)
}

/**
 * A binding key that returns a [[ListBinding]] through `bindList`.
 */
trait ListBindingKey extends BindingKey {
  def bindList[T: NodeMapper]: ListBinding[T] = ListBinding(this)
}

/**
 * A binding key that returns a [[SetBinding]] through `bindSet`.
 */
trait SetBindingKey extends BindingKey {
  def bindSet[T: NodeMapper]: SetBinding[T] = SetBinding(this)
}

/**
 * A binding key that returns an [[IRIBinding]] through `bindIRI`.
 */
trait IRIBindingKey extends BindingKey {
  def bindIRI[T: IRIValueMapper]: IRIBinding[T] = IRIBinding[T](this)

  // multiple IRIs are needed for @type, for example
  def bindIRIs[T: IRIValueMapper]: IRIsBinding[T] = IRIsBinding[T](this)
}

/**
 * A binding key that returns an [[IndexMapBinding]].
 */
trait IndexMapBindingKey extends BindingKey {
  def bindIndexMap: IndexMapBinding =
    IndexMapBinding(this)
}

/**
 * A binding key that returns an [[IdMapBinding]].
 */
trait IdMapBindingKey extends BindingKey {
  def bindIdMap: IdMapBinding =
    IdMapBinding(this)
}

/**
 * A binding key that returns an [[TypeMapBinding]].
 */
trait TypeMapBindingKey extends BindingKey {
  def bindTypeMap: TypeMapBinding =
    TypeMapBinding(this)
}

/**
 * A binding key that returns a [[LanguageMapBinding]].
 */
trait LanguageMapBindingKey extends BindingKey {
  def bindLanguageMap: LanguageMapBinding = LanguageMapBinding(this)
}
