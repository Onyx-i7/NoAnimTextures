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
        if (basicClass == null) return null;
        
        ClassReader classReader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("updateAnimation") && method.desc.equals("()V")) {
                
                InsnList checkList = new InsnList();
                LabelNode allowAnimation = new LabelNode();
                LabelNode cancelAnimation = new LabelNode();
                
                // 1. If disableAnimations is false, skip to allowAnimation (let Minecraft do its thing)
                checkList.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/onyxi7/noanimtextures/ModConfig", "disableAnimations", "Z"));
                checkList.add(new JumpInsnNode(Opcodes.IFEQ, allowAnimation));
                
                // 2. If keepCompassClockAnimated is false, jump to cancelAnimation (cancel ALL)
                checkList.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/onyxi7/noanimtextures/ModConfig", "keepCompassClockAnimated", "Z"));
                checkList.add(new JumpInsnNode(Opcodes.IFEQ, cancelAnimation));
                
                // 3. Get the icon name: String iconName = this.getIconName();
                checkList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                checkList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/texture/TextureAtlasSprite", "getIconName", "()Ljava/lang/String;", false));
                checkList.add(new VarInsnNode(Opcodes.ASTORE, 1)); // Store in local variable 1
                
                // 4. If the name is null, cancel the animation for safety reasons
                checkList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                checkList.add(new JumpInsnNode(Opcodes.IFNULL, cancelAnimation));
                
                // 5. If it contains “compass,” allow animation
                checkList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                checkList.add(new LdcInsnNode("compass"));
                checkList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false));
                checkList.add(new JumpInsnNode(Opcodes.IFNE, allowAnimation));
                
                // 6. If it contains “clock,” allow animation
                checkList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                checkList.add(new LdcInsnNode("clock"));
                checkList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false));
                checkList.add(new JumpInsnNode(Opcodes.IFNE, allowAnimation));
                
                // 7. Cancel tag: Returns immediately (prevents the original method from executing)
                checkList.add(cancelAnimation);
                checkList.add(new InsnNode(Opcodes.RETURN));
                
                // 8. Allow tag: Keep the original Minecraft code
                checkList.add(allowAnimation);
                
                // Inject all of this at the beginning of the original method
                method.instructions.insertBefore(method.instructions.getFirst(), checkList);
                
                System.out.println("[NoAnimTextures] Successfully injected animation control!");
                break;
            }
        }
        
        // COMPUTE_FRAMES is essential to prevent the injected bytecode from corrupting the class
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
