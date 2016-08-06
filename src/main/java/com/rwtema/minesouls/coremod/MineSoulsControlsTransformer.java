package com.rwtema.minesouls.coremod;

import com.google.common.collect.Sets;
import java.util.ListIterator;
import java.util.Set;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MineSoulsControlsTransformer implements IClassTransformer {
	Set<String> isHandActive = Sets.newHashSet("isHandActive", "func_184587_cr");
	Set<String> onLivingUpdate = Sets.newHashSet("onLivingUpdate", "func_70636_d");
	Set<String> attackTargetEntityWithCurrentItem = Sets.newHashSet("attackTargetEntityWithCurrentItem", "func_71059_n");
	Set<String> getCooledAttackStrength = Sets.newHashSet("getCooledAttackStrength", "func_184825_o");
	Set<String> onUpdate = Sets.newHashSet("onUpdate", "func_189213_a");
	Set<String> resetCooldown = Sets.newHashSet("resetCooldown", "func_184821_cY");
	Set<String> attackEntity = Sets.newHashSet("attackEntity", "func_78764_a");
	Set<String> clickMouse = Sets.newHashSet("clickMouse", "func_147116_af");
	Set<String> swingArm = Sets.newHashSet("swingArm", "func_184609_a");
	Set<String> sendClickBlockToController = Sets.newHashSet("sendClickBlockToController", "func_147115_a");
	Set<String> addStatsActual = Sets.newHashSet("addStats", "func_75122_a");
	Set<String> addStatsItem = Sets.newHashSet("addStats", "func_151686_a");
	Set<String> onItemRightClick = Sets.newHashSet("onItemRightClick", "func_77659_a");
	Set<String> canEat = Sets.newHashSet("canEat", "func_71043_e");
	Set<String> onItemUseFinish = Sets.newHashSet("onItemUseFinish", "func_77654_b");
	Set<String> rayTrace = Sets.newHashSet("rayTrace", "func_174822_a");
	Set<String> getMouseOver = Sets.newHashSet("getMouseOver", "func_78473_a");
	Set<String> updatePlayerMoveState = Sets.newHashSet("updatePlayerMoveState", "func_78898_a" );

	@Override
	public byte[] transform(String s, String s1, byte[] bytes) {
		bytes = swapMethodCalls(s, bytes, "net.minecraft.client.entity.EntityPlayerSP",
				onLivingUpdate, null, isHandActive,
				"shouldSlowPlayer", "(Lnet/minecraft/entity/player/EntityPlayer;)Z");
		bytes = swapMethodCalls(s, bytes, "net.minecraft.entity.player.EntityPlayer",
				attackTargetEntityWithCurrentItem, null, getCooledAttackStrength,
				"getAttackStrengthModifier", "(Lnet/minecraft/entity/player/EntityPlayer;F)F");

		bytes = swapMethodCalls(s, bytes, "net.minecraft.entity.player.EntityPlayer",
				onUpdate, null, resetCooldown,
				"dontResetCooldown", "(Lnet/minecraft/entity/player/EntityPlayer;)V");

		bytes = swapMethodCalls(s, bytes, "net.minecraft.client.Minecraft",
				clickMouse, null, attackEntity,
				"attackEntity", "(Lnet/minecraft/client/multiplayer/PlayerControllerMP;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V");

		bytes = swapMethodCalls(s, bytes, "net.minecraft.client.Minecraft",
				clickMouse, null, resetCooldown,
				"dontResetCooldown", "(Lnet/minecraft/entity/player/EntityPlayer;)V");

		bytes = swapMethodCalls(s, bytes, "net.minecraft.client.Minecraft",
				clickMouse, null, swingArm,
				"swingArmIfFull", "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/util/EnumHand;)V");

		bytes = swapMethodCalls(s, bytes, "net.minecraft.client.Minecraft",
				sendClickBlockToController, null, isHandActive,
				"isPlayerUsingOrHoldingSword", "(Lnet/minecraft/entity/player/EntityPlayer;)Z");

		bytes = swapMethodCalls(s, bytes, "net.minecraft.client.Minecraft",
				sendClickBlockToController, null, isHandActive,
				"isPlayerUsingOrHoldingSword", "(Lnet/minecraft/entity/player/EntityPlayer;)Z");

		bytes = swapMethodCalls(s, bytes, "net.minecraft.item.ItemFood",
				onItemUseFinish, null, addStatsItem,
				"onFoodEaten", "(Lnet/minecraft/util/FoodStats;Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;)V"
		);

		bytes = swapMethodCalls(s, bytes, "net.minecraft.item.ItemFood",
				onItemRightClick, null, canEat,
				"canPlayerEat", "(Lnet/minecraft/entity/player/EntityPlayer;Z)Z"
		);

		bytes = swapMethodCalls(s, bytes, "net.minecraft.client.entity.EntityPlayerSP",
				onLivingUpdate, null, updatePlayerMoveState,
				"handleMovementInput", "(Lnet/minecraft/util/MovementInput;)V");



		return bytes;
	}

	private byte[] swapMethodCalls(String s, byte[] bytes, String baseClazz, Set<String> baseMethodNames, String baseMethodDesc, Set<String> baseMethodCalls, String destMethod, String destDescription) {
		if (!baseClazz.equals(s)) return bytes;

		boolean replaceMethod = false;

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		for (MethodNode method : classNode.methods) {
			if (baseMethodNames.contains(method.name) && (baseMethodDesc == null || method.desc.equals(baseMethodDesc))) {
				ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
				int lineNumber = -1;

				while (iter.hasNext()) {
					AbstractInsnNode next = iter.next();
					if (next.getType() == AbstractInsnNode.LINE) {
						lineNumber = ((LineNumberNode) next).line;
					} else if (next.getType() == AbstractInsnNode.METHOD_INSN) {
						MethodInsnNode node = (MethodInsnNode) next;
						if (baseMethodCalls.contains(node.name)) {
							iter.set(
									new MethodInsnNode(
											Opcodes.INVOKESTATIC,
											"com/rwtema/minesouls/MineSoulsControllerHooks",
											destMethod,
											destDescription,
											false
									)
							);
							StringBuilder builder = new StringBuilder();

							builder.append("Replaced call to ").append(getClassName(node.owner)).append(".").append(node.name).append("()");
							builder.append(" with call to com.rwtema.minesouls.MineSoulsControllerHooks.").append(destMethod).append("()");
							builder.append(" in ").append(s).append(".").append(method.name).append("()");
							if (lineNumber != -1) {
								builder.append(" (line ").append(lineNumber).append(")");
							}
							MineSoulsControlsCoreMod.logger.trace(builder.toString());
							replaceMethod = true;
						}
					}
				}

			}
		}
		if (!replaceMethod) {
			MineSoulsControlsCoreMod.logger.warn("Did not replace any calls in " + baseClazz + " : " + baseMethodNames.toString() + ". This is not expected.");
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public String getClassName(String s) {
		String[] split = s.split("/");
		return split[split.length - 1];
	}

}
