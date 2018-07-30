/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.lazy

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.DeclarationStubGenerator
import org.jetbrains.kotlin.ir.util.TypeTranslator
import org.jetbrains.kotlin.ir.util.transform
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.utils.SmartList

class IrLazyPropertyImpl(
    startOffset: Int,
    endOffset: Int,
    origin: IrDeclarationOrigin,
    override val descriptor: PropertyDescriptor,
    override val name: Name,
    override val visibility: Visibility,
    override val modality: Modality,
    override val isVar: Boolean,
    override val isConst: Boolean,
    override val isLateinit: Boolean,
    override val isDelegated: Boolean,
    override val isExternal: Boolean,
    stubGenerator: DeclarationStubGenerator,
    typeTranslator: TypeTranslator
) : IrLazyDeclarationBase(startOffset, endOffset, origin, stubGenerator, typeTranslator),
    IrProperty, IrTypeParametersContainer {

    constructor(
        startOffset: Int,
        endOffset: Int,
        origin: IrDeclarationOrigin,
        isDelegated: Boolean,
        descriptor: PropertyDescriptor,
        stubGenerator: DeclarationStubGenerator,
        typeTranslator: TypeTranslator
    ) : this(
        startOffset, endOffset, origin, descriptor,
        descriptor.name, descriptor.visibility, descriptor.modality,
        isVar = descriptor.isVar,
        isConst = descriptor.isConst,
        isLateinit = descriptor.isLateInit,
        isDelegated = isDelegated,
        isExternal = descriptor.isEffectivelyExternal(),
        stubGenerator = stubGenerator,
        typeTranslator = typeTranslator
    )

    override val typeParameters: MutableList<IrTypeParameter> = SmartList()
    override var backingField: IrField? = null
    override var getter: IrSimpleFunction? = null
    override var setter: IrSimpleFunction? = null

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitProperty(this, data)
    }

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        typeParameters.forEach { it.accept(visitor, data) }
        backingField?.accept(visitor, data)
        getter?.accept(visitor, data)
        setter?.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        typeParameters.transform { it.transform(transformer, data) }
        backingField = backingField?.transform(transformer, data) as? IrField
        getter = getter?.run { transform(transformer, data) as IrSimpleFunction }
        setter = setter?.run { transform(transformer, data) as IrSimpleFunction }
    }
}