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
        System.out.println("[NoAnimTextures] Starting TextureMap transformation...");
        
        if (basicClass == null) {
            System.out.println("[NoAnimTextures] ERROR: basicClass is null!");
            return null;
        }
        
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        
        System.out.println("[NoAnimTextures] Searching for tickAnimations method...");
        
        for (MethodNode method : classNode.methods) {
            if (method.desc.equals("()V") && containsAnimationLoop(method)) {
                System.out.println("[NoAnimTextures] Found tickAnimations method: " + method.name);
                
                method.instructions.clear();
                if (method.tryCatchBlocks != null) {
                    method.tryCatchBlocks.clear();
                }
                if (method.visibleLocalVariableAnnotations != null) {
                    method.visibleLocalVariableAnnotations.clear();
                }
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                
                System.out.println("[NoAnimTextures] tickAnimations replaced with empty method!");
                
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);
                System.out.println("[NoAnimTextures] Transformation complete!");
                return classWriter.toByteArray();
            }
        }
        
        System.out.println("[NoAnimTextures] WARNING: tickAnimations method not found!");
        return basicClass;
    }
    
    private boolean containsAnimationLoop(MethodNode method) {
        boolean hasIterator = false;
        boolean hasInvokeVirtualVoid = false;
        int invokeCount = 0;
        
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                
                if (methodInsn.name.equals("hasNext") || methodInsn.name.equals("next")) {
                    hasIterator = true;
                }
                
                if (methodInsn.desc.equals("()V") && insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    hasInvokeVirtualVoid = true;
                    invokeCount++;
                }
            }
        }
        
        return hasIterator && hasInvokeVirtualVoid && invokeCount >= 1;
    }
}
