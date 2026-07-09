package com.uncraftbar.easyautocycler.mixin;

import com.uncraftbar.easyautocycler.AutomationManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleMerchantOffers", at = @At("TAIL"))
    private void easyAutoCycler$afterMerchantOffersApplied(ClientboundMerchantOffersPacket packet, CallbackInfo ci) {
        AutomationManager.INSTANCE.onMerchantOffersUpdated(packet.getContainerId());
    }
}
