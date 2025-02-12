package dev.hilla.parser.models;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.hilla.parser.utils.StreamUtils;

import io.github.classgraph.TypeParameter;

final class TypeParameterSourceModel extends AbstractModel<TypeParameter>
        implements TypeParameterModel, SourceSignatureModel {
    private List<AnnotationInfoModel> annotations;
    private List<SignatureModel> bounds;

    public TypeParameterSourceModel(TypeParameter origin, Model parent) {
        super(origin, Objects.requireNonNull(parent));
    }

    @Override
    public List<AnnotationInfoModel> getAnnotations() {
        if (annotations == null) {
            annotations = List.of();
        }

        return annotations;
    }

    @Override
    public List<SignatureModel> getBounds() {
        if (bounds == null) {
            bounds = StreamUtils
                    .combine(Stream.of(origin.getClassBound()),
                            origin.getInterfaceBounds().stream())
                    .map(signature -> signature != null
                            ? SignatureModel.of(signature, this)
                            : null)
                    .distinct().collect(Collectors.toList());
        }

        return bounds;
    }
}
