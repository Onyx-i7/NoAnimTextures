package com.onyxi7.noanimtextures.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class TextureAnimationTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.texture.TextureAtlasSprite")) {
            return transformTextureAtlasSprite(basicClass);
        }
        return basicClass;
    }
    
    private byte[] transformTextureAtlasSprite(byte[] basicClass) {
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("updateAnimation") && method.desc.equals("()V")) {
                method.instructions.clear();
                
                InsnList instructions = new InsnList();
                
                instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/onyxi7/noanimtextures/Config", "disableAnimations", "Z"));
                
                LabelNode label = new LabelNode();
                instructions.add(new JumpInsnNode(Opcodes.IFEQ, label));
                
                instructions.add(new InsnNode(Opcodes.RETURN));
                
                instructions.add(label);
                instructions.add(new InsnNode(Opcodes.RETURN));
                
                method.instructions = instructions;
                
                System.out.println("[NoAnimTextures] Successfully injected texture animation control!");
                break;
            }
        }
        
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}