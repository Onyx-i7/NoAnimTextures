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
        
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    
                    if (methodInsn.owner.equals("cdq") && methodInsn.name.equals("j") && methodInsn.desc.equals("()V")) {
                        System.out.println("[NoAnimTextures] Found cdq.j call in method " + method.name + " - removing it!");
                        method.instructions.remove(insn);
                        
                        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        classNode.accept(classWriter);
                        System.out.println("[NoAnimTextures] Transformation complete!");
                        return classWriter.toByteArray();
                    }
                }
            }
        }
        
        System.out.println("[NoAnimTextures] WARNING: cdq.j not found!");
        return basicClass;
    }
}
