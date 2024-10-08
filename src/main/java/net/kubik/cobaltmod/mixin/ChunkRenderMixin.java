package net.kubik.cobaltmod.mixin;

import net.kubik.cobaltmod.Cobalt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

/**
 * Mixin class for BuiltChunk to modify chunk rendering behavior.
 * Improved for better performance at higher chunk distances.
 */
@Mixin(BuiltChunk.class)
public class ChunkRenderMixin {

    @Inject(method = "shouldBuild", at = @At("HEAD"), cancellable = true)
    private void onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
        BuiltChunk thisChunk = (BuiltChunk) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(thisChunk.getOrigin());
		
        // Optimization: Calculate these once outside the loop
        int renderDistanceChunks = client.options.getViewDistance().getValue();
		int size = renderDistanceChunks * 2 + 1;
		int maxChunkIndex = size * size - 1; // Calculate max index once

        // Check for null instead of world/player.
		if (Cobalt.getChunksToRender() == null) return;


        int playerChunkX = client.player.getChunkPos().x;
        int playerChunkZ = client.player.getChunkPos().z;
        
        int chunkIndex = calculateChunkIndex(chunkPos, renderDistanceChunks, playerChunkX, playerChunkZ);

		if (chunkIndex < 0 || chunkIndex > maxChunkIndex) {
			cir.setReturnValue(false);
			return;
		}
		
        cir.setReturnValue(Cobalt.getChunksToRender().get(chunkIndex));
    }

	// Helper function for better readability and avoiding repeated calculations.
	private int calculateChunkIndex(ChunkPos chunkPos, int renderDistance, int playerChunkX, int playerChunkZ){
		int localX = chunkPos.x - (playerChunkX - renderDistance);
		int localZ = chunkPos.z - (playerChunkZ - renderDistance);
		return localX + localZ * (renderDistance * 2 + 1);
	}
}
