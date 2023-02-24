/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2019 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.api.skill.experience.modifier;

import de.Keyle.MyPet.api.Configuration.LevelSystem.Experience.Modifier;
import de.Keyle.MyPet.api.entity.MyPet;
import org.bukkit.entity.Player;

public class PermissionModifier extends ExperienceModifier {

    MyPet myPet;

    public PermissionModifier(MyPet myPet) {
        this.myPet = myPet;
    }

    public double modify(double experience, double baseExperience) {
        if (Modifier.PERMISSION) {
            Player owner = myPet.getOwner().getPlayer();
            if (owner != null) {
                if (owner.hasPermission("MyPet.experience.multiplier.200")) {
                    experience *= 2.00;
                } else if (owner.hasPermission("MyPet.experience.multiplier.199")) {
                    experience *= 1.99;
                } else if (owner.hasPermission("MyPet.experience.multiplier.198")) {
                    experience *= 1.98;
                } else if (owner.hasPermission("MyPet.experience.multiplier.197")) {
                    experience *= 1.97;
                } else if (owner.hasPermission("MyPet.experience.multiplier.196")) {
                    experience *= 1.96;
                } else if (owner.hasPermission("MyPet.experience.multiplier.195")) {
                    experience *= 1.95;
                } else if (owner.hasPermission("MyPet.experience.multiplier.194")) {
                    experience *= 1.94;
                } else if (owner.hasPermission("MyPet.experience.multiplier.193")) {
                    experience *= 1.93;
                } else if (owner.hasPermission("MyPet.experience.multiplier.192")) {
                    experience *= 1.92;
                } else if (owner.hasPermission("MyPet.experience.multiplier.191")) {
                    experience *= 1.91;
                } else if (owner.hasPermission("MyPet.experience.multiplier.190")) {
                    experience *= 1.90;
                } else if (owner.hasPermission("MyPet.experience.multiplier.189")) {
                    experience *= 1.89;
                } else if (owner.hasPermission("MyPet.experience.multiplier.188")) {
                    experience *= 1.88;
                } else if (owner.hasPermission("MyPet.experience.multiplier.187")) {
                    experience *= 1.87;
                } else if (owner.hasPermission("MyPet.experience.multiplier.186")) {
                    experience *= 1.86;
                } else if (owner.hasPermission("MyPet.experience.multiplier.185")) {
                    experience *= 1.85;
                } else if (owner.hasPermission("MyPet.experience.multiplier.184")) {
                    experience *= 1.84;
                } else if (owner.hasPermission("MyPet.experience.multiplier.183")) {
                    experience *= 1.83;
                } else if (owner.hasPermission("MyPet.experience.multiplier.182")) {
                    experience *= 1.82;
                } else if (owner.hasPermission("MyPet.experience.multiplier.181")) {
                    experience *= 1.81;
                } else if (owner.hasPermission("MyPet.experience.multiplier.180")) {
                    experience *= 1.80;
                } else if (owner.hasPermission("MyPet.experience.multiplier.179")) {
                    experience *= 1.79;
                } else if (owner.hasPermission("MyPet.experience.multiplier.178")) {
                    experience *= 1.78;
                } else if (owner.hasPermission("MyPet.experience.multiplier.177")) {
                    experience *= 1.77;
                } else if (owner.hasPermission("MyPet.experience.multiplier.176")) {
                    experience *= 1.76;
                } else if (owner.hasPermission("MyPet.experience.multiplier.175")) {
                    experience *= 1.75;
                } else if (owner.hasPermission("MyPet.experience.multiplier.174")) {
                    experience *= 1.74;
                } else if (owner.hasPermission("MyPet.experience.multiplier.173")) {
                    experience *= 1.73;
                } else if (owner.hasPermission("MyPet.experience.multiplier.172")) {
                    experience *= 1.72;
                } else if (owner.hasPermission("MyPet.experience.multiplier.171")) {
                    experience *= 1.71;
                } else if (owner.hasPermission("MyPet.experience.multiplier.170")) {
                    experience *= 1.70;
                } else if (owner.hasPermission("MyPet.experience.multiplier.169")) {
                    experience *= 1.69;
                } else if (owner.hasPermission("MyPet.experience.multiplier.168")) {
                    experience *= 1.68;
                } else if (owner.hasPermission("MyPet.experience.multiplier.167")) {
                    experience *= 1.67;
                } else if (owner.hasPermission("MyPet.experience.multiplier.166")) {
                    experience *= 1.66;
                } else if (owner.hasPermission("MyPet.experience.multiplier.165")) {
                    experience *= 1.65;
                } else if (owner.hasPermission("MyPet.experience.multiplier.164")) {
                    experience *= 1.64;
                } else if (owner.hasPermission("MyPet.experience.multiplier.163")) {
                    experience *= 1.63;
                } else if (owner.hasPermission("MyPet.experience.multiplier.162")) {
                    experience *= 1.62;
                } else if (owner.hasPermission("MyPet.experience.multiplier.161")) {
                    experience *= 1.61;
                } else if (owner.hasPermission("MyPet.experience.multiplier.160")) {
                    experience *= 1.60;
                } else if (owner.hasPermission("MyPet.experience.multiplier.159")) {
                    experience *= 1.59;
                } else if (owner.hasPermission("MyPet.experience.multiplier.158")) {
                    experience *= 1.58;
                } else if (owner.hasPermission("MyPet.experience.multiplier.157")) {
                    experience *= 1.57;
                } else if (owner.hasPermission("MyPet.experience.multiplier.156")) {
                    experience *= 1.56;
                } else if (owner.hasPermission("MyPet.experience.multiplier.155")) {
                    experience *= 1.55;
                } else if (owner.hasPermission("MyPet.experience.multiplier.154")) {
                    experience *= 1.54;
                } else if (owner.hasPermission("MyPet.experience.multiplier.153")) {
                    experience *= 1.53;
                } else if (owner.hasPermission("MyPet.experience.multiplier.152")) {
                    experience *= 1.52;
                } else if (owner.hasPermission("MyPet.experience.multiplier.151")) {
                    experience *= 1.51;
                } else if (owner.hasPermission("MyPet.experience.multiplier.150")) {
                    experience *= 1.50;
                } else if (owner.hasPermission("MyPet.experience.multiplier.149")) {
                    experience *= 1.49;
                } else if (owner.hasPermission("MyPet.experience.multiplier.148")) {
                    experience *= 1.48;
                } else if (owner.hasPermission("MyPet.experience.multiplier.147")) {
                    experience *= 1.47;
                } else if (owner.hasPermission("MyPet.experience.multiplier.146")) {
                    experience *= 1.46;
                } else if (owner.hasPermission("MyPet.experience.multiplier.145")) {
                    experience *= 1.45;
                } else if (owner.hasPermission("MyPet.experience.multiplier.144")) {
                    experience *= 1.44;
                } else if (owner.hasPermission("MyPet.experience.multiplier.143")) {
                    experience *= 1.43;
                } else if (owner.hasPermission("MyPet.experience.multiplier.142")) {
                    experience *= 1.42;
                } else if (owner.hasPermission("MyPet.experience.multiplier.141")) {
                    experience *= 1.41;
                } else if (owner.hasPermission("MyPet.experience.multiplier.140")) {
                    experience *= 1.40;
                } else if (owner.hasPermission("MyPet.experience.multiplier.139")) {
                    experience *= 1.39;
                } else if (owner.hasPermission("MyPet.experience.multiplier.138")) {
                    experience *= 1.38;
                } else if (owner.hasPermission("MyPet.experience.multiplier.137")) {
                    experience *= 1.37;
                } else if (owner.hasPermission("MyPet.experience.multiplier.136")) {
                    experience *= 1.36;
                } else if (owner.hasPermission("MyPet.experience.multiplier.135")) {
                    experience *= 1.35;
                } else if (owner.hasPermission("MyPet.experience.multiplier.134")) {
                    experience *= 1.34;
                } else if (owner.hasPermission("MyPet.experience.multiplier.133")) {
                    experience *= 1.33;
                } else if (owner.hasPermission("MyPet.experience.multiplier.132")) {
                    experience *= 1.32;
                } else if (owner.hasPermission("MyPet.experience.multiplier.131")) {
                    experience *= 1.31;
                } else if (owner.hasPermission("MyPet.experience.multiplier.130")) {
                    experience *= 1.3;
                } else if (owner.hasPermission("MyPet.experience.multiplier.129")) {
                    experience *= 1.29;
                } else if (owner.hasPermission("MyPet.experience.multiplier.128")) {
                    experience *= 1.28;
                } else if (owner.hasPermission("MyPet.experience.multiplier.127")) {
                    experience *= 1.27;
                } else if (owner.hasPermission("MyPet.experience.multiplier.126")) {
                    experience *= 1.26;
                } else if (owner.hasPermission("MyPet.experience.multiplier.125")) {
                    experience *= 1.25;
                } else if (owner.hasPermission("MyPet.experience.multiplier.124")) {
                    experience *= 1.24;
                } else if (owner.hasPermission("MyPet.experience.multiplier.123")) {
                    experience *= 1.23;
                } else if (owner.hasPermission("MyPet.experience.multiplier.122")) {
                    experience *= 1.22;
                } else if (owner.hasPermission("MyPet.experience.multiplier.121")) {
                    experience *= 1.21;
                } else if (owner.hasPermission("MyPet.experience.multiplier.120")) {
                    experience *= 1.20;
                } else if (owner.hasPermission("MyPet.experience.multiplier.119")) {
                    experience *= 1.19;
                } else if (owner.hasPermission("MyPet.experience.multiplier.118")) {
                    experience *= 1.18;
                } else if (owner.hasPermission("MyPet.experience.multiplier.117")) {
                    experience *= 1.17;
                } else if (owner.hasPermission("MyPet.experience.multiplier.116")) {
                    experience *= 1.16;
                } else if (owner.hasPermission("MyPet.experience.multiplier.115")) {
                    experience *= 1.15;
                } else if (owner.hasPermission("MyPet.experience.multiplier.114")) {
                    experience *= 1.14;
                } else if (owner.hasPermission("MyPet.experience.multiplier.113")) {
                    experience *= 1.13;
                } else if (owner.hasPermission("MyPet.experience.multiplier.112")) {
                    experience *= 1.12;
                } else if (owner.hasPermission("MyPet.experience.multiplier.111")) {
                    experience *= 1.11;
                } else if (owner.hasPermission("MyPet.experience.multiplier.110")) {
                    experience *= 1.1;
                } else if (owner.hasPermission("MyPet.experience.multiplier.109")) {
                    experience *= 1.09;
                } else if (owner.hasPermission("MyPet.experience.multiplier.108")) {
                    experience *= 1.08;
                } else if (owner.hasPermission("MyPet.experience.multiplier.107")) {
                    experience *= 1.07;
                } else if (owner.hasPermission("MyPet.experience.multiplier.106")) {
                    experience *= 1.06;
                } else if (owner.hasPermission("MyPet.experience.multiplier.105")) {
                    experience *= 1.05;
                } else if (owner.hasPermission("MyPet.experience.multiplier.104")) {
                    experience *= 1.04;
                } else if (owner.hasPermission("MyPet.experience.multiplier.103")) {
                    experience *= 1.03;
                } else if (owner.hasPermission("MyPet.experience.multiplier.102")) {
                    experience *= 1.02;
                } else if (owner.hasPermission("MyPet.experience.multiplier.101")) {
                    experience *= 1.01;
                }
            }
        }
        return experience;
    }
}