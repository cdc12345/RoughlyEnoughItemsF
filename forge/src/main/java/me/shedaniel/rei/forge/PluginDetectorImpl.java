/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.plugin.client.DefaultClientPlugin;
import me.shedaniel.rei.plugin.client.DefaultClientRuntimePlugin;
import me.shedaniel.rei.plugin.common.DefaultPlugin;
import me.shedaniel.rei.plugin.common.DefaultRuntimePlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PluginDetectorImpl {
    private static <P extends me.shedaniel.rei.api.common.plugins.REIPlugin<?>> REIPluginProvider<P> wrapPlugin(List<String> modId, REIPluginProvider<P> plugin) {
        return new REIPluginProvider<P>() {
            String nameSuffix = " [" + String.join(", ", modId) + "]";
            
            @Override
            public Collection<P> provide() {
                return plugin.provide();
            }
            
            @Override
            public Class<P> getPluginProviderClass() {
                return plugin.getPluginProviderClass();
            }
            
            @Override
            public String getPluginProviderName() {
                return plugin.getPluginProviderName() + nameSuffix;
            }
        };
    }
    
    public static void detectServerPlugins() {
        PluginView.getServerInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultPlugin()));
        PluginView.getServerInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultRuntimePlugin()));
        AnnotationUtils.<REIPlugin, REIServerPlugin>scanAnnotation(REIPlugin.class, REIServerPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
            ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
        });
    }
    
    public static void detectCommonPlugins() {
        EventBuses.registerModEventBus("roughlyenoughitems", FMLJavaModLoadingContext.get().getModEventBus());
        AnnotationUtils.<REIPlugin, me.shedaniel.rei.api.common.plugins.REIPlugin<?>>scanAnnotation(REIPlugin.class, me.shedaniel.rei.api.common.plugins.REIPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
            ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void detectClientPlugins() {
        PluginView.getClientInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultClientPlugin()));
        PluginView.getClientInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultClientRuntimePlugin()));
        AnnotationUtils.<REIPlugin, REIClientPlugin>scanAnnotation(REIPlugin.class, REIClientPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
            ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
        });
        ClientInternals.attachInstance((Supplier<List<String>>) ArrayList::new, "jeiCompatMods");
    }
}
