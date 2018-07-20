package choice_types.int_choice;

subtype int8  VariantA;
subtype int16 VariantB;
subtype int32 VariantC;

choice IntChoice(uint16 tag) on tag
{
    case 1:
        VariantA  a;

    case 2:
    case 3:
    case 4:
        VariantB  b;

    case 5:
    case 6:
        // empty
        ;

    default:
        VariantC  c;
};
