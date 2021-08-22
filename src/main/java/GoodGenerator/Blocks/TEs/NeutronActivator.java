package GoodGenerator.Blocks.TEs;

import GoodGenerator.Blocks.TEs.MetaTE.NeutronAccelerator;
import GoodGenerator.Blocks.TEs.MetaTE.NeutronSensor;
import GoodGenerator.Client.GUI.NeutronActivatorGUIClient;
import GoodGenerator.Common.Container.NeutronActivatorGUIContainer;
import GoodGenerator.Loader.Loaders;
import GoodGenerator.util.CharExchanger;
import GoodGenerator.util.DescTextLocalization;
import GoodGenerator.util.ItemRefer;
import GoodGenerator.util.MyRecipeAdder;
import com.github.technus.tectech.mechanics.constructable.IConstructable;
import com.github.technus.tectech.mechanics.structure.IStructureDefinition;
import com.github.technus.tectech.mechanics.structure.StructureDefinition;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.GT_MetaTileEntity_MultiblockBase_EM;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import ic2.core.Ic2Items;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collection;

import static GoodGenerator.util.StructureHelper.addFrame;
import static com.github.technus.tectech.mechanics.structure.StructureUtility.*;
import static org.apache.commons.lang3.RandomUtils.nextInt;

public class NeutronActivator extends GT_MetaTileEntity_MultiblockBase_EM implements IConstructable {

    protected IStructureDefinition<NeutronActivator> multiDefinition = null;
    protected final ArrayList<NeutronAccelerator> mNeutronAccelerator = new ArrayList<>();
    protected final ArrayList<NeutronSensor> mNeutronSensor = new ArrayList<>();
    protected int casingAmount = 0;
    protected int eV = 0, mCeil = 0, mFloor = 0;

    private static final IIconContainer textureFontOn = new Textures.BlockIcons.CustomIcon("icons/NeutronActivator_On");
    private static final IIconContainer textureFontOn_Glow = new Textures.BlockIcons.CustomIcon("icons/NeutronActivator_On_GLOW");
    private static final IIconContainer textureFontOff = new Textures.BlockIcons.CustomIcon("icons/NeutronActivator_Off");
    private static final IIconContainer textureFontOff_Glow = new Textures.BlockIcons.CustomIcon("icons/NeutronActivator_Off_GLOW");

    public NeutronActivator(String name) {
        super(name);
    }

    public NeutronActivator(int id, String name, String nameRegional) {
        super(id, name, nameRegional);
    }

    @Override
    public boolean checkRecipe_EM(ItemStack aStack) {
        this.mEfficiency = 10000;

        ArrayList<FluidStack> tFluids = getStoredFluids();
        ArrayList<ItemStack> tItems = getStoredInputs();
        Collection<GT_Recipe> tRecipes = MyRecipeAdder.instance.NA.mRecipeList;

        for (int i = 0; i < tFluids.size() - 1; i++) {
            for (int j = i + 1; j < tFluids.size(); j++) {
                if (GT_Utility.areFluidsEqual(tFluids.get(i), tFluids.get(j))) {
                    if ((tFluids.get(i)).amount >= (tFluids.get(j)).amount) {
                        tFluids.remove(j--);
                    } else {
                        tFluids.remove(i--);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < tItems.size() - 1; i++) {
            for (int j = i + 1; j < tItems.size(); j++) {
                if (GT_Utility.areStacksEqual(tItems.get(i), tItems.get(j))) {
                    if ((tItems.get(i)).stackSize >= (tItems.get(j)).stackSize) {
                        tItems.remove(j--);
                    } else {
                        tItems.remove(i--);
                        break;
                    }
                }
            }
        }

        FluidStack[] inFluids = tFluids.toArray(new FluidStack[0]);
        ItemStack[] inItems = tItems.toArray(new ItemStack[0]);
        int minNKE, maxNKE;

        for (GT_Recipe recipe : tRecipes) {
            minNKE = (recipe.mSpecialValue % 10000) * 1000000;
            maxNKE = (recipe.mSpecialValue / 10000) * 1000000;
            mFloor = minNKE;
            mCeil = maxNKE;
            if (recipe.isRecipeInputEqual(true, inFluids, inItems)) {
                mMaxProgresstime = recipe.mDuration;
                if (eV <= maxNKE && eV >= minNKE) {
                    this.mOutputFluids = recipe.mFluidOutputs;
                    this.mOutputItems = recipe.mOutputs;
                }
                else {
                    this.mOutputFluids = null;
                    this.mOutputItems = new ItemStack[]{ItemRefer.Radioactive_Waste.get(4)};
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getPollutionPerTick(ItemStack aStack) {
        return 0;
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new NeutronActivatorGUIClient(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "EMDisplay.png");
    }

    @Override
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new NeutronActivatorGUIContainer(aPlayerInventory, aBaseMetaTileEntity);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        eV = aNBT.getInteger("mKeV");
        mCeil = aNBT.getInteger("mCeil");
        mFloor = aNBT.getInteger("mFloor");
        super.loadNBTData(aNBT);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setInteger("mKeV", eV);
        aNBT.setInteger("mCeil", mCeil);
        aNBT.setInteger("mFloor", mFloor);
        super.saveNBTData(aNBT);
    }

    @Override
    public String[] getDescription() {
        final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("Neutron Activator")
                .addInfo("Controller block for the Neutron Activator")
                .addInfo("Superluminal-velocity Motion.")
                .addInfo("You need to input energy to the Neutron Accelerator to get it running.")
                .addInfo("It will output correct products with Specific Neutron Kinetic Energy.")
                .addInfo("Otherwise it will output trash.")
                .addInfo("The Neutron Kinetic Energy will decrease 72KeV/s when no Neutron Accelerator is running.")
                .addInfo("It will explode when the Neutron Kinetic Energy is over" + EnumChatFormatting.RED + " 1200MeV" + EnumChatFormatting.GRAY + ".")
                .addInfo("The structure is too complex!")
                .addInfo("Follow the" + EnumChatFormatting.DARK_BLUE + " Tec" + EnumChatFormatting.BLUE + "Tech" + EnumChatFormatting.GRAY + " blueprint to build the main structure.")
                .addSeparator()
                .beginStructureBlock(5, 6, 5, false)
                .addController("Front bottom")
                .addInputHatch("Hint block with dot 1")
                .addInputBus("Hint block with dot 1")
                .addOutputHatch("Hint block with dot 2")
                .addOutputBus("Hint block with dot 2")
                .addMaintenanceHatch("Hint block with dot 2")
                .addOtherStructurePart("Neutron Accelerator", "Hint block with dot 2")
                .addOtherStructurePart("Neutron Sensor", "Hint block with dot 2")
                .addCasingInfo("Clean Stainless Steel Machine Casing", 7)
                .toolTipFinisher("Good Generator");
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            return tt.getInformation();
        } else {
            return tt.getStructureInformation();
        }
    }

    @Override
    public IStructureDefinition<NeutronActivator> getStructure_EM() {
        if (multiDefinition == null) {
            multiDefinition = StructureDefinition
                    .<NeutronActivator>builder()
                    .addShape(mName,
                            transpose(new String[][]{
                                    {"CCCCC", "CDDDC", "CDDDC", "CDDDC", "CCCCC"},
                                    {"F   F", " GGG ", " GPG ", " GGG ", "F   F"},
                                    {"F   F", " GGG ", " GPG ", " GGG ", "F   F"},
                                    {"F   F", " GGG ", " GPG ", " GGG ", "F   F"},
                                    {"F   F", " GGG ", " GPG ", " GGG ", "F   F"},
                                    {"XX~XX", "XDDDX", "XDDDX", "XDDDX", "XXXXX"},
                            })
                    )
                    .addElement(
                            'C',
                            ofChain(
                                    ofHatchAdder(
                                            NeutronActivator::addClassicInputToMachineList, 49,
                                            1
                                    ),
                                    onElementPass(
                                            x -> x.casingAmount++,
                                            ofBlock(
                                                    GregTech_API.sBlockCasings4, 1
                                            )
                                    )
                            )
                    )
                    .addElement(
                            'D',
                            ofBlock(
                                    GregTech_API.sBlockCasings2, 6
                            )
                    )
                    .addElement(
                            'F',
                            addFrame(
                                    Materials.Steel
                            )
                    )
                    .addElement(
                            'G',
                            ofBlock(
                                    Block.getBlockFromItem(Ic2Items.reinforcedGlass.getItem()), 0
                            )
                    )
                    .addElement(
                            'P',
                            ofBlock(
                                    Loaders.speedingPipe, 0
                            )
                    )
                    .addElement(
                            'X',
                            ofChain(
                                    ofHatchAdder(
                                            NeutronActivator::addClassicOutputToMachineList, 49,
                                            2
                                    ),
                                    ofHatchAdder(
                                            NeutronActivator::addMaintenanceToMachineList, 49,
                                            2
                                    ),
                                    ofHatchAdder(
                                            NeutronActivator::addAcceleratorAndSensor, 49,
                                            2
                                    ),
                                    onElementPass(
                                            x -> x.casingAmount++,
                                            ofBlock(
                                                    GregTech_API.sBlockCasings4, 1
                                            )
                                    )
                            )
                    )
                    .build();
        }
        return multiDefinition;
    }

    @Override
    public boolean checkMachine_EM(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        this.casingAmount = 0;
        this.mNeutronAccelerator.clear();
        this.mNeutronSensor.clear();
        return structureCheck_EM(mName, 2, 5, 0) && casingAmount >= 7;
    }

    public final boolean addAcceleratorAndSensor(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) {
            return false;
        } else {
            IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
            if (aMetaTileEntity instanceof NeutronAccelerator) {
                ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
                return this.mNeutronAccelerator.add((NeutronAccelerator) aMetaTileEntity);
            } else if (aMetaTileEntity instanceof NeutronSensor) {
                ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
                return this.mNeutronSensor.add((NeutronSensor) aMetaTileEntity);
            }
        }
        return false;
    }

    public int maxNeutronKineticEnergy() {
        return 1200000000;
    }

    public int getCurrentNeutronKineticEnergy() {
        return eV;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new NeutronActivator(this.mName);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        boolean anyWorking = false;
        if (this.getBaseMetaTileEntity().isServerSide()) {
            for (NeutronAccelerator tHatch : mNeutronAccelerator) {
                if (tHatch.isRunning && this.getRepairStatus() == this.getIdealStatus()) {
                    anyWorking = true;
                    this.eV += nextInt(tHatch.getMaxEUConsume(), tHatch.getMaxEUConsume() * 2 + 1) * 10;
                }
            }
            if (!anyWorking) {
                if (this.eV >= 72000 && aTick % 20 == 0) {
                    this.eV -= 72000;
                } else if (this.eV > 0 && aTick % 20 == 0) {
                    this.eV = 0;
                }
            }
            if (this.eV < 0) this.eV = 0;
            if (this.eV > maxNeutronKineticEnergy()) doExplosion(4 * 32);

            for (NeutronSensor tHatch : mNeutronSensor) {
                String tText = tHatch.getText();
                if (CharExchanger.isValidCompareExpress(rawProcessExp(tText))) {
                    if (CharExchanger.compareExpression(rawProcessExp(tText), eV)) {
                        tHatch.outputRedstoneSignal();
                    } else tHatch.stopOutputRedstoneSignal();
                }
            }

            if (mProgresstime < mMaxProgresstime && (eV > mCeil || eV < mFloor)) {
                this.mOutputFluids = null;
                this.mOutputItems = new ItemStack[]{ItemRefer.Radioactive_Waste.get(4)};
            }
        }
    }

    protected String rawProcessExp(String exp) {
        StringBuilder ret = new StringBuilder();
        for (char c : exp.toCharArray()) {
            if (exp.length() - ret.length() == 3) {
                if (Character.isDigit(c)) ret.append(c);
                else {
                    if (c == 'K' || c == 'k') {
                        ret.append("000");
                    }
                    if (c == 'M' || c == 'm') {
                        ret.append("000000");
                    }
                }
                break;
            }
            ret.append(c);
        }
        return ret.toString();
    }

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        structureBuild_EM(mName, 2, 5, 0, b, itemStack);
    }

    @Override
    public String[] getStructureDescription(ItemStack itemStack) {
        return DescTextLocalization.addText("NeutronActivator.hint", 7);
    }

    @Override
    public String[] getInfoData() {
        int currentNKEInput = 0;
        boolean anyWorking = false;
        for (NeutronAccelerator tHatch : mNeutronAccelerator) {
            if (tHatch.isRunning) {
                currentNKEInput += nextInt(tHatch.getMaxEUConsume(), tHatch.getMaxEUConsume() * 2 + 1) * 10;
                anyWorking = true;
            }
        }
        if (!anyWorking) currentNKEInput = -72000;
        return new String[] {
                "Progress:",
                EnumChatFormatting.GREEN + Integer.toString(this.mProgresstime / 20) + EnumChatFormatting.RESET + " s / " + EnumChatFormatting.YELLOW + this.mMaxProgresstime / 20 + EnumChatFormatting.RESET + " s",
                "Current Neutron Kinetic Energy Input: " + EnumChatFormatting.GREEN + GT_Utility.formatNumbers(currentNKEInput) + EnumChatFormatting.RESET + "eV",
                StatCollector.translateToLocal("scanner.info.NA") + " " + EnumChatFormatting.LIGHT_PURPLE + GT_Utility.formatNumbers(getCurrentNeutronKineticEnergy()) + EnumChatFormatting.RESET + "eV"
        };
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if(aSide == aFacing) {
            if(aActive) return new ITexture[]{
                    Textures.BlockIcons.getCasingTextureForId(49),
                    TextureFactory.of(textureFontOn),
                    TextureFactory.builder().addIcon(textureFontOn_Glow).glow().build()
            };
            else return new ITexture[]{
                    Textures.BlockIcons.getCasingTextureForId(49),
                    TextureFactory.of(textureFontOff),
                    TextureFactory.builder().addIcon(textureFontOff_Glow).glow().build()
            };
        }
        return new ITexture[]{Textures.BlockIcons.getCasingTextureForId(49)};
    }
}
