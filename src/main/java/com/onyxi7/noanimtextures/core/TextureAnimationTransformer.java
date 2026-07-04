package com.onyxi7.noanimtextures.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class TextureAnimationTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.texture.TextureAtlasSprite")) {
            System.out.println("[NoAnimTextures] Transforming TextureAtlasSprite!");
            return transformTextureAtlasSprite(basicClass);
        }
        return basicClass;
    }
    
    private byte[] transformTextureAtlasSprite(byte[] basicClass) {
        System.out.println("[NoAnimTextures] Starting transformation...");
        
        if (basicClass == null) {
            System.out.println("[NoAnimTextures] ERROR: basicClass is null!");
            return null;
        }
        
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        
        System.out.println("[NoAnimTextures] Class loaded, searching for updateAnimation method...");
        
        for (MethodNode method : classNode.methods) {
            System.out.println("[NoAnimTextures] Found method: " + method.name + method.desc);
            
            if (method.name.equals("updateAnimation") && method.desc.equals("()V")) {
                System.out.println("[NoAnimTextures] Found updateAnimation! Replacing with empty method...");
                
                method.instructions.clear();
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                
                System.out.println("[NoAnimTextures] Method replaced successfully!");
                
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);
                System.out.println("[NoAnimTextures] Transformation complete!");
                return classWriter.toByteArray();
            }
        }
        
        System.out.println("[NoAnimTextures] WARNING: updateAnimation method not found!");
        return basicClass;
    }
}
