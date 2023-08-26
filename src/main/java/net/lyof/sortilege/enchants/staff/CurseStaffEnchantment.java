package net.lyof.sortilege.enchants.staff;

public class CurseStaffEnchantment extends StaffEnchantment {
    public CurseStaffEnchantment(Rarity pRarity) {
        super(pRarity, 1);
    }

    @Override
    public boolean isCurse() {
        return true;
    }
}
