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
        
        System.out.println("[NoAnimTextures] Analyzing ALL methods in TextureMap...");
        
        // Buscar TODOS los métodos que llaman a TextureAtlasSprite
        for (MethodNode method : classNode.methods) {
            boolean callsSpriteMethod = false;
            String calledMethod = "";
            
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    
                    // Buscar cualquier llamada a TextureAtlasSprite (nombre SRG o normal)
                    if (methodInsn.owner.equals("net/minecraft/client/renderer/texture/TextureAtlasSprite") ||
                        methodInsn.owner.equals("cdq") ||
                        methodInsn.owner.equals("bua")) {
                        
                        System.out.println("[NoAnimTextures] Method " + method.name + " calls " + methodInsn.owner + "." + methodInsn.name);
                        callsSpriteMethod = true;
                        calledMethod = methodInsn.name;
                    }
                }
            }
            
            // Si este método llama a un método void de TextureAtlasSprite, eliminar esa llamada
            if (callsSpriteMethod && calledMethod.startsWith("func_") && calledMethod.endsWith("_l")) {
                System.out.println("[NoAnimTextures] Found method with sprite call: " + method.name + " -> " + calledMethod);
                
                int removedCalls = 0;
                AbstractInsnNode insn = method.instructions.getFirst();
                while (insn != null) {
                    AbstractInsnNode next = insn.getNext();
                    
                    if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if ((methodInsn.owner.equals("net/minecraft/client/renderer/texture/TextureAtlasSprite") ||
                             methodInsn.owner.equals("cdq") ||
                             methodInsn.owner.equals("bua")) &&
                            methodInsn.name.equals(calledMethod) &&
                            methodInsn.desc.equals("()V")) {
                            
                            System.out.println("[NoAnimTextures] Removing call to " + methodInsn.name);
                            method.instructions.remove(insn);
                            removedCalls++;
                        }
                    }
                    insn = next;
                }
                
                if (removedCalls > 0) {
                    System.out.println("[NoAnimTextures] Removed " + removedCalls + " calls!");
                }
            }
        }
        
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        System.out.println("[NoAnimTextures] Transformation complete!");
        return classWriter.toByteArray();
    }
}
