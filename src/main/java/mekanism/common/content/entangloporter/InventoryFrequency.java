package mekanism.common.content.entangloporter;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.frequency.Frequency;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class InventoryFrequency extends Frequency
{
	public static final double MAX_ENERGY = 1000000;
	
	public double storedEnergy;
	public FluidTank storedFluid = new FluidTank(1000);
	public GasTank storedGas = new GasTank(1000);
	public ItemStack storedItem;
	public double temperature;
	
	public Map<TransmissionType, TransferType> transmissions = new HashMap<TransmissionType, TransferType>();
	
	public InventoryFrequency(String n, String o)
	{
		super(n, o);
	}
	
	public InventoryFrequency(NBTTagCompound nbtTags)
	{
		super(nbtTags);
	}
	
	public InventoryFrequency(ByteBuf dataStream)
	{
		super(dataStream);
	}
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setDouble("storedEnergy", storedEnergy);
		
		if(storedFluid != null)
		{
			nbtTags.setTag("storedFluid", storedFluid.writeToNBT(new NBTTagCompound()));
		}
		
		if(storedGas != null)
		{
			nbtTags.setTag("storedGas", storedGas.write(new NBTTagCompound()));
		}
		
		if(storedItem != null)
		{
			nbtTags.setTag("storedItem", storedItem.writeToNBT(new NBTTagCompound()));
		}
		
		nbtTags.setDouble("temperature", temperature);
		
		for(TransmissionType type : TransmissionType.values())
		{
			nbtTags.setInteger("transmission" + type.ordinal(), transmissions.get(type).ordinal());
		}
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		storedEnergy = nbtTags.getDouble("storedEnergy");
		
		if(nbtTags.hasKey("storedFluid"))
		{
			storedFluid.readFromNBT(nbtTags.getCompoundTag("storedFluid"));
		}
		
		if(nbtTags.hasKey("storedGas"))
		{
			storedGas.read(nbtTags.getCompoundTag("storedGas"));
		}
		
		if(nbtTags.hasKey("storedItem"))
		{
			storedItem.readFromNBT(nbtTags.getCompoundTag("storedItem"));
		}
		
		temperature = nbtTags.getDouble("temperature");
		
		for(TransmissionType type : TransmissionType.values())
		{
			transmissions.put(type, TransferType.values()[nbtTags.getInteger("transmission" + type.ordinal())]);
		}
	}

	@Override
	public void write(ArrayList data)
	{
		super.write(data);
		
		data.add(storedEnergy);
		
		if(storedFluid.getFluid() != null)
		{
			data.add(true);
			data.add(storedFluid.getFluid().getFluidID());
			data.add(storedFluid.getFluidAmount());
		}
		else {
			data.add(false);
		}
		
		if(storedGas.getGas() != null)
		{
			data.add(true);
			data.add(storedGas.getGasType().getID());
			data.add(storedGas.getStored());
		}
		else {
			data.add(false);
		}
		
		data.add(temperature);
		
		for(TransmissionType type : TransmissionType.values())
		{
			data.add(transmissions.get(type).ordinal());
		}
	}

	@Override
	protected void read(ByteBuf dataStream)
	{
		super.read(dataStream);
		
		storedEnergy = dataStream.readDouble();
		
		if(dataStream.readBoolean())
		{
			storedFluid.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			storedFluid.setFluid(null);
		}
		
		if(dataStream.readBoolean())
		{
			storedGas.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			storedGas.setGas(null);
		}
		
		temperature = dataStream.readDouble();
		
		for(TransmissionType type : TransmissionType.values())
		{
			transmissions.put(type, TransferType.values()[dataStream.readInt()]);
		}
	}
	
	public static enum TransferType
	{
		RECEIVE,
		SEND,
		BOTH;
	}
}
