package service;

import data.SettingsDAO;
import domain.Settings;

// Handles Maintenance Mode ON/OFF using the 'settings' table in ERP DB.
public class MaintenanceService {

    private static final String MAINTENANCE_KEY = "maintenance_mode";

    private final SettingsDAO settingsDAO;

    public MaintenanceService() {
        this.settingsDAO = new SettingsDAO();
    }

    // For dependency injection 
    public MaintenanceService(SettingsDAO dao) {
        this.settingsDAO = dao;
    }

    // check if maintenance mode is currently ON
    public boolean isMaintenanceOn() throws Exception {
        Settings s = settingsDAO.getSetting(MAINTENANCE_KEY);
        if (s == null) {
            return false; // is OFF
        }
        return s.asBoolean();
    }

    // Turn maintenance mode ON.
    public void enableMaintenance() {
        Settings s = new Settings(MAINTENANCE_KEY, "true");
        settingsDAO.saveOrUpdate(s);
    }

    // Turn maintenance mode OFF.
    public void disableMaintenance() {
        Settings s = new Settings(MAINTENANCE_KEY, "false");
        settingsDAO.saveOrUpdate(s);
    }

    // Toggle maintenance mode.
    public boolean toggleMaintenance() throws Exception {
        boolean current = isMaintenanceOn();
        boolean newValue = !current;

        Settings s = new Settings(MAINTENANCE_KEY, newValue ? "true" : "false");
        settingsDAO.saveOrUpdate(s);

        return newValue; // return the updated state
    }
}