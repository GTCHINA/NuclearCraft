package nc.recipe.multiblock;

import static nc.config.NCConfig.*;
import static nc.util.FluidStackHelper.INGOT_VOLUME;

import java.util.*;

import nc.recipe.BasicRecipeHandler;

public class SaltFissionRecipes extends BasicRecipeHandler {
	
	public SaltFissionRecipes() {
		super("salt_fission", 0, 1, 0, 1);
	}
	
	@Override
	public void addRecipes() {
		addFuelDepleteRecipes(fission_thorium_fuel_time, fission_thorium_heat_generation, fission_thorium_efficiency, fission_thorium_criticality, fission_thorium_decay_factor, fission_thorium_self_priming, fission_thorium_radiation, "tbu");
		addFuelDepleteRecipes(fission_uranium_fuel_time, fission_uranium_heat_generation, fission_uranium_efficiency, fission_uranium_criticality, fission_uranium_decay_factor, fission_uranium_self_priming, fission_uranium_radiation, "leu_233", "heu_233", "leu_235", "heu_235");
		addFuelDepleteRecipes(fission_neptunium_fuel_time, fission_neptunium_heat_generation, fission_neptunium_efficiency, fission_neptunium_criticality, fission_neptunium_decay_factor, fission_neptunium_self_priming, fission_neptunium_radiation, "len_236", "hen_236");
		addFuelDepleteRecipes(fission_plutonium_fuel_time, fission_plutonium_heat_generation, fission_plutonium_efficiency, fission_plutonium_criticality, fission_plutonium_decay_factor, fission_plutonium_self_priming, fission_plutonium_radiation, "lep_239", "hep_239", "lep_241", "hep_241");
		addFuelDepleteRecipes(fission_mixed_fuel_time, fission_mixed_heat_generation, fission_mixed_efficiency, fission_mixed_criticality, fission_mixed_decay_factor, fission_mixed_self_priming, fission_mixed_radiation, "mix_239", "mix_241");
		addFuelDepleteRecipes(fission_americium_fuel_time, fission_americium_heat_generation, fission_americium_efficiency, fission_americium_criticality, fission_americium_decay_factor, fission_americium_self_priming, fission_americium_radiation, "lea_242", "hea_242");
		addFuelDepleteRecipes(fission_curium_fuel_time, fission_curium_heat_generation, fission_curium_efficiency, fission_curium_criticality, fission_curium_decay_factor, fission_curium_self_priming, fission_curium_radiation, "lecm_243", "hecm_243", "lecm_245", "hecm_245", "lecm_247", "hecm_247");
		addFuelDepleteRecipes(fission_berkelium_fuel_time, fission_berkelium_heat_generation, fission_berkelium_efficiency, fission_berkelium_criticality, fission_berkelium_decay_factor, fission_berkelium_self_priming, fission_berkelium_radiation, "leb_248", "heb_248");
		addFuelDepleteRecipes(fission_californium_fuel_time, fission_californium_heat_generation, fission_californium_efficiency, fission_californium_criticality, fission_californium_decay_factor, fission_californium_self_priming, fission_californium_radiation, "lecf_249", "hecf_249", "lecf_251", "hecf_251");
	}
	
	public void addFuelDepleteRecipes(int[] time, int[] heat, double[] efficiency, int[] criticality, double[] decayFactor, boolean[] selfPriming, double[] radiation, String... fuelTypes) {
		int id = 0;
		for (String fuelType : fuelTypes) {
			addRecipe(fluidStack(fuelType + "_fluoride_flibe", 1), fluidStack("depleted_" + fuelType + "_fluoride_flibe", 1), (double) time[id + 4] / (double) INGOT_VOLUME, heat[id + 4], efficiency[id + 4], criticality[id + 4], decayFactor[id + 4], selfPriming[id + 4], radiation[id + 4]);
			id += 5;
		}
	}
	
	@Override
	protected void setStats() {}
	
	@Override
	protected List<Object> fixedExtras(List<Object> extras) {
		ExtrasFixer fixer = new ExtrasFixer(extras);
		fixer.add(Double.class, 1D);
		fixer.add(Integer.class, 0);
		fixer.add(Double.class, 0D);
		fixer.add(Integer.class, 1);
		fixer.add(Double.class, 0D);
		fixer.add(Boolean.class, false);
		fixer.add(Double.class, 0D);
		return fixer.fixed;
	}
	
	@Override
	public List<Object> getFactoredExtras(List<Object> extras, int factor) {
		List<Object> factored = new ArrayList<>(extras);
		factored.set(0, (int) extras.get(0) / factor);
		return factored;
	}
}
