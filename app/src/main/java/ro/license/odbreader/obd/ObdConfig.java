package ro.license.odbreader.obd;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.control.EquivalentRatioCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.control.TimingAdvanceCommand;
import com.github.pires.obd.commands.control.VinCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.FuelRailPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;

import java.util.ArrayList;

public final class ObdConfig {

    public static ArrayList<ObdCommand> getCommands() {
        ArrayList<ObdCommand> cmds = new ArrayList<>();

        // obdsim available commands
        cmds.add(new LoadCommand());
        cmds.add(new RPMCommand());
        cmds.add(new MassAirFlowCommand());
        cmds.add(new ThrottlePositionCommand());
        cmds.add(new EngineCoolantTemperatureCommand());
        cmds.add(new SpeedCommand());

        // Control
        cmds.add(new ModuleVoltageCommand());
        cmds.add(new EquivalentRatioCommand());
        cmds.add(new DistanceMILOnCommand());
        cmds.add(new TimingAdvanceCommand());
        cmds.add(new VinCommand());

        // Engine
        cmds.add(new RuntimeCommand());

        // Fuel
        cmds.add(new FindFuelTypeCommand());
        cmds.add(new ConsumptionRateCommand());
        cmds.add(new FuelLevelCommand());
        cmds.add(new AirFuelRatioCommand());
        cmds.add(new WidebandAirFuelRatioCommand());
        cmds.add(new OilTempCommand());

        // Pressure
        cmds.add(new BarometricPressureCommand());
        cmds.add(new FuelPressureCommand());
        cmds.add(new FuelRailPressureCommand());
        cmds.add(new IntakeManifoldPressureCommand());

        // Temperature
        cmds.add(new AirIntakeTemperatureCommand());
        cmds.add(new AmbientAirTemperatureCommand());

        return cmds;
    }

}
