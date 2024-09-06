package net.lyof.sortilege.enchant.staff;

public class CurseStaffEnchantment extends StaffEnchantment {
    public CurseStaffEnchantment(Rarity pRarity) {
        super(pRarity, 1);
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
