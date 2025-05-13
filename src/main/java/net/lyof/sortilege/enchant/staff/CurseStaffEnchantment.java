package net.lyof.sortilege.enchant.staff;

public class CurseStaffEnchantment extends StaffEnchantment {
    public CurseStaffEnchantment(Rarity rarity) {
        super(rarity, 1);
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
}
