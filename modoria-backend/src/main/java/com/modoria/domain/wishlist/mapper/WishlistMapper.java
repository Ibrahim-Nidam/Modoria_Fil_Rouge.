package com.modoria.domain.wishlist.mapper;

import com.modoria.domain.product.mapper.ProductMapper;
import com.modoria.domain.wishlist.dto.response.WishlistItemResponse;
import com.modoria.domain.wishlist.entity.WishlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { ProductMapper.class })
public interface WishlistMapper {

    @Mapping(target = "product", source = "product")
    @Mapping(target = "productVariant", source = "productVariant")
    WishlistItemResponse toResponse(WishlistItem wishlistItem);
}
