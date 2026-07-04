package com.onyxi7.noanimtextures.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class TextureAnimationTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.texture.TextureMap")) {
            System.out.println("[NoAnimTextures] Transforming TextureMap!");
            return transformTextureMap(basicClass);
        }
        return basicClass;
    }
    
    private byte[] transformTextureMap(byte[] basicClass) {
        if (basicClass == null) return null;
        
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        
        System.out.println("[NoAnimTextures] Searching for animation tick method...");
        
        for (MethodNode method : classNode.methods) {
            if (method.desc.equals("()V") && iteratesOverSprites(method)) {
                System.out.println("[NoAnimTextures] Found animation method: " + method.name);
                
                // Eliminar todas las llamadas a updateAnimation
                int removedCalls = 0;
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    AbstractInsnNode next = insn.getNext();
                    
                    if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        // Buscar llamadas a updateAnimation en TextureAtlasSprite
                        if (methodInsn.owner.equals("net/minecraft/client/renderer/texture/TextureAtlasSprite") &&
                            methodInsn.desc.equals("()V") &&
                            (methodInsn.name.equals("func_94219_l") || methodInsn.name.equals("updateAnimation"))) {
                            
                            System.out.println("[NoAnimTextures] Removing call to " + methodInsn.name);
                            method.instructions.remove(insn);
                            removedCalls++;
                        }
                    }
                    
                    insn = next;
                }
                
                if (removedCalls > 0) {
                    System.out.println("[NoAnimTextures] Removed " + removedCalls + " updateAnimation calls!");
                }
                
                break;
            }
        }
        
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        System.out.println("[NoAnimTextures] Transformation complete!");
        return classWriter.toByteArray();
    }
    
    // Verificar si el método itera sobre una lista de sprites
    private boolean iteratesOverSprites(MethodNode method) {
        boolean hasIterator = false;
        boolean hasSpriteField = false;
        
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                // Buscar iteradores
                if (methodInsn.name.equals("hasNext") || methodInsn.name.equals("next")) {
                    hasIterator = true;
                }
            }
            
            if (insn.getType() == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                // Buscar acceso al campo de sprites
                if (fieldInsn.desc.equals("Ljava/util/List;") || 
                    fieldInsn.desc.equals("Ljava/util/ArrayList;")) {
                    hasSpriteField = true;
                }
            }
        }
        
        return hasIterator && hasSpriteField;
    }
}
